package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.InMemoryOperationEventRepository;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InMemoryCentralPortabilityPort;
import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the start of a portability operation.
 *
 * <p>The use case is idempotent (BR-07): the same key with the same payload answers the
 * operation that already exists, the same key with another payload is a conflict, and
 * the loser of a race answers with the operation of the winner. The eligibility of the
 * installments comes from the agenda (BR-03) and the amounts are frozen in the snapshot
 * (BR-05). The operation is sent to the Central and waits for the confirmation (BR-08):
 * a refusal of the Central leaves it resumable, in {@code INTEGRATION_ERROR} (BR-11).
 */
class StartOperationTest {

  private static final Instant NOW = Instant.parse("2026-11-27T10:15:30Z");

  private static final Duration BACKOFF = Duration.ofSeconds(60);

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");
  private static final IdempotencyKey KEY = new IdempotencyKey(UUID.randomUUID());

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryOperationEventRepository events;
  private InMemoryCentralPortabilityPort central;
  private StartOperation useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    events = new InMemoryOperationEventRepository();
    central = new InMemoryCentralPortabilityPort();
    useCase = useCase(operations, central);
  }

  @Test
  void createsTheOperationWithTheFrozenSnapshot() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    PortabilityOperation operation = result.operation();
    assertThat(result.replay()).isFalse();
    assertThat(operation.createdAt()).isEqualTo(NOW);
    assertThat(operation.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
    assertThat(operation.installments())
        .extracting(snapshot -> snapshot.installmentId().value() + "|" + snapshot.amount().amount())
        .containsExactly("PAR-001|450.00", "PAR-002|300.00");
  }

  @Test
  void sendsTheOperationToTheCentralAndWaitsForTheConfirmation() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    PortabilityOperation operation = result.operation();
    assertThat(operation.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(operation.protocol()).isEqualTo(InMemoryCentralPortabilityPort.PROTOCOL);
    assertThat(central.submissions()).isEqualTo(1);
    assertThat(central.lastSubmission().id()).isEqualTo(operation.id());
  }

  @Test
  void appendsTheCreationEvent() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    List<OperationEvent> trail = events.findByOperationId(result.operation().id());
    assertThat(trail).hasSize(2);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CREATED);
    assertThat(trail.get(0).previousStatus()).isNull();
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CREATED);
  }

  @Test
  void appendsTheSubmissionEvent() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    List<OperationEvent> trail = events.findByOperationId(result.operation().id());
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.OPERATION_SUBMITTED);
    assertThat(trail.get(1).previousStatus()).isEqualTo(OperationStatus.CREATED);
    assertThat(trail.get(1).newStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(1).details()).contains(InMemoryCentralPortabilityPort.PROTOCOL);
  }

  @Test
  void answersTheExistingOperationWhenTheKeyIsRepeated() {
    StartOperation.StartOperationResult first = useCase.execute(request(), KEY);

    StartOperation.StartOperationResult second = useCase.execute(request(), KEY);

    assertThat(second.replay()).isTrue();
    assertThat(second.operation()).isEqualTo(first.operation());
    assertThat(operations.size()).isEqualTo(1);
    assertThat(central.submissions()).isEqualTo(1);
    assertThat(events.findByOperationId(first.operation().id())).hasSize(2);
  }

  @Test
  void refusesTheKeyWhenThePayloadIsAnother() {
    useCase.execute(request(), KEY);
    OperationRequest divergent =
        new OperationRequest(
            BENEFICIARY,
            AGREEMENT,
            List.of(new InstallmentId("PAR-001")),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"));

    assertThatThrownBy(() -> useCase.execute(divergent, KEY))
        .isInstanceOf(DomainError.IdempotencyConflict.class);
  }

  @Test
  void refusesAClosedInstallment() {
    OperationRequest request =
        new OperationRequest(
            BENEFICIARY,
            AGREEMENT,
            List.of(new InstallmentId("PAR-003")),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"));

    assertThatThrownBy(() -> useCase.execute(request, KEY))
        .isInstanceOf(DomainError.InstallmentNotEligible.class)
        .hasMessageContaining("PAR-003");
  }

  @Test
  void refusesAnInstallmentOfAnotherAgreement() {
    OperationRequest request =
        new OperationRequest(
            BENEFICIARY,
            new AgreementId("AGR-002"),
            List.of(new InstallmentId("PAR-001")),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"));

    assertThatThrownBy(() -> useCase.execute(request, KEY))
        .isInstanceOf(DomainError.InstallmentNotEligible.class);
  }

  @Test
  void refusesAnInstallmentThatTheAgendaDoesNotKnow() {
    OperationRequest request =
        new OperationRequest(
            BENEFICIARY,
            AGREEMENT,
            List.of(new InstallmentId("PAR-404")),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"));

    assertThatThrownBy(() -> useCase.execute(request, KEY))
        .isInstanceOf(DomainError.InstallmentNotEligible.class);
  }

  @Test
  void leavesTheOperationResumableWhenTheCentralFails() {
    central.failWith(new CentralSubmissionException(503, "CENTRAL_INDISPONIVEL", "unavailable"));

    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    PortabilityOperation operation = result.operation();
    assertThat(operation.status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(operation.protocol()).isNull();
    assertThat(operation.retryCount()).isEqualTo(1);
    assertThat(operation.nextPollAt()).isEqualTo(NOW.plus(BACKOFF));
  }

  @Test
  void appendsTheIntegrationFailureEvent() {
    central.failWith(new CentralSubmissionException(503, "CENTRAL_INDISPONIVEL", "unavailable"));

    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    List<OperationEvent> trail = events.findByOperationId(result.operation().id());
    assertThat(trail).hasSize(2);
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
    assertThat(trail.get(1).previousStatus()).isEqualTo(OperationStatus.CREATED);
    assertThat(trail.get(1).newStatus()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(trail.get(1).details()).contains("CENTRAL_INDISPONIVEL");
  }

  @Test
  void doesNotSendAgainAnOperationThatFailed() {
    central.failWith(new CentralSubmissionException(503, "CENTRAL_INDISPONIVEL", "unavailable"));
    useCase.execute(request(), KEY);

    StartOperation.StartOperationResult second = useCase.execute(request(), KEY);

    assertThat(second.replay()).isTrue();
    assertThat(second.operation().status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(central.submissions()).isEqualTo(1);
  }

  @Test
  void completesTheSubmissionOfAnOperationThatWasNeverSent() {
    PortabilityOperation orphan =
        PortabilityOperation.create(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            KEY,
            BENEFICIARY,
            AGREEMENT,
            List.of(
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"),
                    AGREEMENT,
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05")),
                new InstallmentSnapshot(
                    new InstallmentId("PAR-002"),
                    AGREEMENT,
                    new Money(new BigDecimal("300.00")),
                    LocalDate.parse("2027-01-05"))),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"),
            NOW);
    operations.save(orphan);

    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    assertThat(result.replay()).isTrue();
    assertThat(result.operation().status()).isEqualTo(OperationStatus.PENDING);
    assertThat(result.operation().protocol()).isEqualTo(InMemoryCentralPortabilityPort.PROTOCOL);
    assertThat(central.submissions()).isEqualTo(1);
  }

  @Test
  void answersTheWinnerWhenTheSaveLosesTheRace() {
    RacingPortabilityOperationRepository racing = new RacingPortabilityOperationRepository();
    StartOperation racingUseCase = useCase(racing, central);
    PortabilityOperation winner = PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.randomUUID()),
        KEY,
        BENEFICIARY,
        AGREEMENT,
        List.of(
            new InstallmentSnapshot(
                new InstallmentId("PAR-001"),
                AGREEMENT,
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05")),
            new InstallmentSnapshot(
                new InstallmentId("PAR-002"),
                AGREEMENT,
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"))),
        "ConsigPort",
        new ReceivingAccount("001", "1234", "56789-0"),
        NOW);
    racing.storeConcurrently(winner);

    StartOperation.StartOperationResult result = racingUseCase.execute(request(), KEY);

    assertThat(result.replay()).isTrue();
    assertThat(result.operation().id()).isEqualTo(winner.id());
  }

  @Test
  void refusesTheKeyWhenTheWinnerStoredAnotherPayload() {
    RacingPortabilityOperationRepository racing = new RacingPortabilityOperationRepository();
    StartOperation racingUseCase = useCase(racing, central);
    PortabilityOperation winner =
        PortabilityOperation.create(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            KEY,
            BENEFICIARY,
            AGREEMENT,
            List.of(
                new InstallmentSnapshot(
                    new InstallmentId("PAR-404"),
                    AGREEMENT,
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05"))),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"),
            NOW);
    racing.storeConcurrently(winner);

    assertThatThrownBy(() -> racingUseCase.execute(request(), KEY))
        .isInstanceOf(DomainError.IdempotencyConflict.class);
  }

  @Test
  void answersTheStoredOperationWhenAnotherTransactionWinsTheRace() {
    ConflictingPortabilityOperationRepository conflicting =
        new ConflictingPortabilityOperationRepository();
    StartOperation conflictingUseCase = useCase(conflicting, central);

    StartOperation.StartOperationResult result = conflictingUseCase.execute(request(), KEY);

    assertThat(result.operation().status()).isEqualTo(OperationStatus.PENDING);
    assertThat(result.operation().protocol()).isEqualTo(InMemoryCentralPortabilityPort.PROTOCOL);
  }

  private StartOperation useCase(
      br.com.consigport.domain.portability.PortabilityOperationRepository repository,
      br.com.consigport.domain.portability.CentralPortabilityPort centralPort) {
    return new StartOperation(
        repository,
        beneficiaryId -> agenda(),
        centralPort,
        events,
        new OperationPolicy(),
        work -> work.run(),
        Clock.fixed(NOW, ZoneOffset.UTC),
        BACKOFF);
  }

  private static OperationRequest request() {
    return new OperationRequest(
        BENEFICIARY,
        AGREEMENT,
        List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
        "ConsigPort",
        new ReceivingAccount("001", "1234", "56789-0"));
  }

  private static BeneficiaryAgenda agenda() {
    return new BeneficiaryAgenda(
        BENEFICIARY,
        List.of(
            new Installment(
                new InstallmentId("PAR-001"),
                AGREEMENT,
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05"),
                InstallmentStatus.OPEN),
            new Installment(
                new InstallmentId("PAR-002"),
                AGREEMENT,
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"),
                InstallmentStatus.OPEN),
            new Installment(
                new InstallmentId("PAR-003"),
                AGREEMENT,
                new Money(new BigDecimal("100.00")),
                LocalDate.parse("2027-02-05"),
                InstallmentStatus.CLOSED)));
  }

  /**
   * Repository that loses the optimistic lock: the creation succeeds and the transition
   * is refused, because another transaction moved the operation forward in the meantime.
   */
  private static final class ConflictingPortabilityOperationRepository
      implements br.com.consigport.domain.portability.PortabilityOperationRepository {

    private final InMemoryPortabilityOperationRepository delegate =
        new InMemoryPortabilityOperationRepository();
    private int saves;

    @Override
    public void save(PortabilityOperation operation) {
      if (saves == 0) {
        saves++;
        delegate.save(operation);
        return;
      }
      delegate.save(operation);
      throw new ConcurrentOperationUpdateException(operation.id());
    }

    @Override
    public Optional<PortabilityOperation> findById(PortabilityOperation.OperationId id) {
      return delegate.findById(id);
    }

    @Override
    public Optional<PortabilityOperation> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
      return delegate.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PortabilityOperation> findByProtocol(String protocol) {
      return delegate.findByProtocol(protocol);
    }

    @Override
    public List<PortabilityOperation> findDueForPolling(Instant now, int limit) {
      return delegate.findDueForPolling(now, limit);
    }

    @Override
    public br.com.consigport.domain.shared.Page<br.com.consigport.domain.portability.OperationSummary>
        list(br.com.consigport.domain.portability.OperationFilter filter, int page, int size) {
      return delegate.list(filter, page, size);
    }

    @Override
    public Optional<br.com.consigport.domain.portability.OperationDetail> findDetailById(
        PortabilityOperation.OperationId id) {
      return delegate.findDetailById(id);
    }
  }

  /**
   * Repository that loses the race: the first lookup sees nothing, another request
   * commits the key in the meantime and the save is refused by the unique constraint.
   */
  private static final class RacingPortabilityOperationRepository
      implements br.com.consigport.domain.portability.PortabilityOperationRepository {

    private final InMemoryPortabilityOperationRepository delegate =
        new InMemoryPortabilityOperationRepository();
    private boolean firstLookup = true;

    void storeConcurrently(PortabilityOperation operation) {
      delegate.save(operation);
    }

    @Override
    public void save(PortabilityOperation operation) {
      Optional<PortabilityOperation> taken =
          delegate.findByIdempotencyKey(operation.idempotencyKey());
      if (taken.isPresent() && !taken.get().id().equals(operation.id())) {
        throw new IdempotencyKeyAlreadyUsedException(operation.idempotencyKey());
      }
      delegate.save(operation);
    }

    @Override
    public Optional<PortabilityOperation> findById(PortabilityOperation.OperationId id) {
      return delegate.findById(id);
    }

    @Override
    public Optional<PortabilityOperation> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
      if (firstLookup) {
        firstLookup = false;
        return Optional.empty();
      }
      return delegate.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<PortabilityOperation> findByProtocol(String protocol) {
      return delegate.findByProtocol(protocol);
    }

    @Override
    public List<PortabilityOperation> findDueForPolling(Instant now, int limit) {
      return delegate.findDueForPolling(now, limit);
    }

    @Override
    public br.com.consigport.domain.shared.Page<br.com.consigport.domain.portability.OperationSummary>
        list(br.com.consigport.domain.portability.OperationFilter filter, int page, int size) {
      return delegate.list(filter, page, size);
    }

    @Override
    public Optional<br.com.consigport.domain.portability.OperationDetail> findDetailById(
        PortabilityOperation.OperationId id) {
      return delegate.findDetailById(id);
    }
  }
}
