package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.InMemoryOperationEventRepository;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.InMemoryCentralPortabilityPort;
import br.com.consigport.domain.portability.InMemoryCentralStatusPort;
import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.portability.RejectionReason;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.support.MutableClock;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the worker that follows the operations until the Central answers.
 *
 * <p>The clock is controlled, so the polling is proven without waiting: the {@code 204}
 * that schedules the next poll with the {@code Retry-After} of the Central, the
 * confirmation, the rejection with its reason, the failure that leaves the operation
 * resumable with the backoff and the operation that never reached the Central, which is
 * sent again with the same idempotency key (BR-07, BR-15).
 */
class PollOperationTest {

  private static final Instant NOW = Instant.parse("2026-11-27T10:15:30Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryOperationEventRepository events;
  private InMemoryCentralPortabilityPort central;
  private InMemoryCentralStatusPort status;
  private MutableClock clock;
  private PollOperation useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    events = new InMemoryOperationEventRepository();
    central = new InMemoryCentralPortabilityPort();
    status = new InMemoryCentralStatusPort();
    clock = new MutableClock(NOW);
    useCase =
        new PollOperation(
            operations,
            central,
            status,
            events,
            work -> work.run(),
            clock,
            new IntegrationBackoff(Duration.ofSeconds(2), Duration.ofSeconds(60)),
            20);
  }

  @Test
  void pollsTheOperationThatIsWaitingForTheConfirmation() {
    operations.save(pending());
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    PollOperation.PollResult result = useCase.pollBatch();

    assertThat(result.polled()).isEqualTo(1);
    assertThat(status.polls()).isEqualTo(1);
    assertThat(status.lastPoll()).isEqualTo(PROTOCOL);
    assertThat(stored().status()).isEqualTo(OperationStatus.CONFIRMED);
  }

  @Test
  void appendsTheConfirmationEventWithTheRawAnswerOfTheCentral() {
    operations.save(pending());
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    useCase.pollBatch();

    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail).hasSize(1);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CONFIRMED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(0).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
  }

  @Test
  void schedulesTheNextPollFromTheAnswerOfTheCentralAndNotFromTheStartOfTheBatch() {
    operations.save(pending());
    status.answerWith(new CentralStatus.Pending(Duration.ofSeconds(2), null));
    // The Central takes 5 s to answer: the next poll is 2 s after the answer, never in
    // the past.
    status.beforeAnswer(() -> clock.advance(Duration.ofSeconds(5)));

    useCase.pollBatch();

    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(5).plusSeconds(2));
  }

  @Test
  void recordsTheAttemptAndHonoursTheRetryAfterOfTheCentral() {
    operations.save(pending());
    status.answerWith(new CentralStatus.Pending(Duration.ofSeconds(5), null));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.PENDING);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(5));
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.POLL_ATTEMPT);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.PENDING);
  }

  @Test
  void recordsTheRejectionWithItsReason() {
    operations.save(pending());
    status.answerWith(
        new CentralStatus.Rejected(
            new RejectionReason.MarginInsufficient(),
            "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"MARGEM_CONSIGNADA_INSUFICIENTE\"}"));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.REJECTED);
    assertThat(stored().rejectionReason()).isEqualTo(new RejectionReason.MarginInsufficient());
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_REJECTED);
    assertThat(trail.get(0).details()).contains("MARGIN_INSUFFICIENT");
  }

  @Test
  void leavesTheOperationResumableWhenTheCentralDoesNotAnswer() {
    operations.save(pending());
    status.fail();

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(stored().retryCount()).isEqualTo(1);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(2));
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
  }

  @Test
  void leavesTheOperationResumableWhenTheCentralDoesNotKnowTheProtocol() {
    operations.save(pending());
    status.answerWith(
        new CentralStatus.NotFound(
            PROTOCOL, "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada.\"}"));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(stored().retryCount()).isEqualTo(1);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(2));
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
    assertThat(trail.get(0).details()).contains(PROTOCOL);
    assertThat(trail.get(0).rawPayload()).contains("OPERACAO_NAO_ENCONTRADA");
  }

  @Test
  void keepsTheOperationInIntegrationErrorWhenTheFailureHappensAgain() {
    operations.save(pending().registerIntegrationFailure(NOW.minusSeconds(1)));
    status.fail();

    useCase.pollBatch();

    // The self-transition declared in ARCHITECTURE.md §7: the status does not change, the
    // count of the failures grows and the backoff is renewed.
    assertThat(stored().status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(stored().retryCount()).isEqualTo(2);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(4));
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
  }

  @Test
  void closesTheOperationWhenTheCentralRejectsWithAReasonTheDomainDoesNotMap() {
    operations.save(pending());
    status.answerWith(
        new CentralStatus.Rejected(
            new RejectionReason.Unmapped(),
            "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"LIMITE_DE_PARCELAS\"}"));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.REJECTED);
    assertThat(stored().rejectionReason()).isEqualTo(new RejectionReason.Unmapped());
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_REJECTED);
    assertThat(trail.get(0).details()).contains("UNMAPPED_REJECTION");
    assertThat(trail.get(0).rawPayload()).contains("LIMITE_DE_PARCELAS");
  }

  @Test
  void doublesTheWaitBetweenTwoFailuresAndCapsItAtTheCeiling() {
    operations.save(pending());
    status.fail();

    useCase.pollBatch();
    assertThat(stored().retryCount()).isEqualTo(1);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(2));

    clock.advance(Duration.ofSeconds(2));
    useCase.pollBatch();

    assertThat(stored().retryCount()).isEqualTo(2);
    // The second wait is 4 s, twice the wait of the first failure.
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(6));
  }

  @Test
  void capsTheWaitAtTheCeilingOfTheBackoff() {
    operations.save(pending());
    status.fail();

    for (int attempt = 0; attempt < 8; attempt++) {
      clock.advance(Duration.ofSeconds(120));
      useCase.pollBatch();
    }

    assertThat(stored().retryCount()).isEqualTo(8);
    assertThat(stored().nextPollAt()).isEqualTo(clock.instant().plusSeconds(60));
  }

  @Test
  void sendsAgainTheOperationThatNeverReachedTheCentral() {
    operations.save(created().registerIntegrationFailure(NOW.minusSeconds(1)));
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    useCase.pollBatch();

    assertThat(central.submissions()).isEqualTo(1);
    assertThat(central.lastSubmission().idempotencyKey()).isEqualTo(pending().idempotencyKey());
    assertThat(stored().status()).isEqualTo(OperationStatus.PENDING);
    assertThat(stored().protocol()).isEqualTo(InMemoryCentralPortabilityPort.PROTOCOL);
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_SUBMITTED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.PENDING);
  }

  @Test
  void keepsTheOperationResumableWhenTheResubmissionFails() {
    operations.save(created().registerIntegrationFailure(NOW.minusSeconds(1)));
    central.failWith(new CentralSubmissionException("the Central is down"));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(stored().retryCount()).isEqualTo(2);
    assertThat(stored().nextPollAt()).isEqualTo(NOW.plusSeconds(4));
    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
  }

  @Test
  void doesNotPollTheOperationWhoseNextPollIsInTheFuture() {
    PortabilityOperation later = pending().pollAttempt(NOW.plusSeconds(30));
    operations.save(later);

    PollOperation.PollResult result = useCase.pollBatch();

    assertThat(result.polled()).isZero();
    assertThat(status.polls()).isZero();
  }

  @Test
  void pollsTheOperationWhoseIntegrationFailedAfterTheCentralAnswered() {
    PortabilityOperation failed = pending().registerIntegrationFailure(NOW.minusSeconds(1));
    operations.save(failed);
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    useCase.pollBatch();

    assertThat(stored().status()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(status.lastPoll()).isEqualTo(PROTOCOL);
  }

  @Test
  void pollsAtMostTheBatchSize() {
    for (int index = 0; index < 25; index++) {
      operations.save(pending(index));
    }
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    PollOperation.PollResult result = useCase.pollBatch();

    assertThat(result.polled()).isEqualTo(20);
    assertThat(status.polls()).isEqualTo(20);
  }

  private PortabilityOperation stored() {
    return operations.findById(pending().id()).orElseThrow();
  }

  private static PortabilityOperation pending() {
    return pending(0);
  }

  private static PortabilityOperation pending(int index) {
    return created(index).submit(PROTOCOL);
  }

  private static PortabilityOperation created() {
    return created(0);
  }

  private static PortabilityOperation created(int index) {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(
            UUID.nameUUIDFromBytes(("operation-" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8))),
        new IdempotencyKey(UUID.nameUUIDFromBytes(("key-" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8))),
        new BeneficiaryId("BEN-001"),
        new AgreementId("AGR-001"),
        List.of(
            new InstallmentSnapshot(
                new InstallmentId("PAR-001"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05")),
            new InstallmentSnapshot(
                new InstallmentId("PAR-002"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"))),
        "ConsigPort",
        new ReceivingAccount("001", "1234", "56789-0"),
        NOW);
  }
}
