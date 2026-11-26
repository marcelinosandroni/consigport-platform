package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.InMemoryOperationEventRepository;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
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
import br.com.consigport.domain.shared.TransactionRunner;
import java.math.BigDecimal;
import java.time.Clock;
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
 * (BR-05).
 */
class StartOperationTest {

  private static final Instant NOW = Instant.parse("2026-11-26T10:15:30Z");

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");
  private static final IdempotencyKey KEY = new IdempotencyKey(UUID.randomUUID());

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryOperationEventRepository events;
  private StartOperation useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    events = new InMemoryOperationEventRepository();
    useCase = new StartOperation(
        operations,
        beneficiaryId -> agenda(),
        events,
        new OperationPolicy(),
        work -> work.run(),
        Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createsTheOperationWithTheFrozenSnapshot() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    PortabilityOperation operation = result.operation();
    assertThat(result.replay()).isFalse();
    assertThat(operation.status()).isEqualTo(OperationStatus.CREATED);
    assertThat(operation.protocol()).isNull();
    assertThat(operation.createdAt()).isEqualTo(NOW);
    assertThat(operation.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
    assertThat(operation.installments())
        .extracting(snapshot -> snapshot.installmentId().value() + "|" + snapshot.amount().amount())
        .containsExactly("PAR-001|450.00", "PAR-002|300.00");
  }

  @Test
  void appendsTheCreationEvent() {
    StartOperation.StartOperationResult result = useCase.execute(request(), KEY);

    List<OperationEvent> trail = events.findByOperationId(result.operation().id());
    assertThat(trail).hasSize(1);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CREATED);
    assertThat(trail.get(0).previousStatus()).isNull();
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CREATED);
  }

  @Test
  void answersTheExistingOperationWhenTheKeyIsRepeated() {
    StartOperation.StartOperationResult first = useCase.execute(request(), KEY);

    StartOperation.StartOperationResult second = useCase.execute(request(), KEY);

    assertThat(second.replay()).isTrue();
    assertThat(second.operation()).isEqualTo(first.operation());
    assertThat(operations.size()).isEqualTo(1);
    assertThat(events.findByOperationId(first.operation().id())).hasSize(1);
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
  void answersTheWinnerWhenTheSaveLosesTheRace() {
    RacingPortabilityOperationRepository racing = new RacingPortabilityOperationRepository();
    StartOperation racingUseCase =
        new StartOperation(
            racing,
            beneficiaryId -> agenda(),
            events,
            new OperationPolicy(),
            work -> work.run(),
            Clock.fixed(NOW, ZoneOffset.UTC));
    PortabilityOperation winner = PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.randomUUID()),
        KEY,
        BENEFICIARY,
        AGREEMENT,
        List.of(
            new br.com.consigport.domain.portability.InstallmentSnapshot(
                new InstallmentId("PAR-001"),
                AGREEMENT,
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05")),
            new br.com.consigport.domain.portability.InstallmentSnapshot(
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
    assertThat(result.operation()).isEqualTo(winner);
  }

  @Test
  void refusesTheKeyWhenTheWinnerStoredAnotherPayload() {
    RacingPortabilityOperationRepository racing = new RacingPortabilityOperationRepository();
    StartOperation racingUseCase =
        new StartOperation(
            racing,
            beneficiaryId -> agenda(),
            events,
            new OperationPolicy(),
            work -> work.run(),
            Clock.fixed(NOW, ZoneOffset.UTC));
    PortabilityOperation winner =
        PortabilityOperation.create(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            KEY,
            BENEFICIARY,
            AGREEMENT,
            List.of(
                new br.com.consigport.domain.portability.InstallmentSnapshot(
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
      if (delegate.findByIdempotencyKey(operation.idempotencyKey()).isPresent()) {
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
  }
}
