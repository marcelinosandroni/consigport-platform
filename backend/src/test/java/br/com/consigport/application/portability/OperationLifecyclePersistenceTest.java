package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.application.agenda.InMemoryAgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.InMemoryCentralPortabilityPort;
import br.com.consigport.domain.portability.InMemoryCentralStatusPort;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.portability.RejectionReason;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.TransactionRunner;
import br.com.consigport.support.FailingOperationEventRepository;
import br.com.consigport.support.MutableClock;
import br.com.consigport.support.PostgreSqlTestSupport;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The whole lifecycle of a portability operation over PostgreSQL 16 (the flow of
 * {@code ARCHITECTURE.md} §11).
 *
 * <p>The use cases drive the scenario against the database of the stack: the operation is
 * created, sent to the Central, polled until the Central answers and closed. Every
 * transition and every event of the trail is written by the real adapters, in the real
 * transactions, so the test proves the whole path — {@code CREATED} to {@code PENDING} to
 * {@code CONFIRMED} to {@code COMPLETED}, and {@code CREATED} to {@code PENDING} to
 * {@code REJECTED} — and not one piece of it at a time.
 *
 * <p>The Central is a fake: the rule of the tests forbids calling a real third-party API
 * in an integration test. The database is real, and that is what this task proves.
 */
@SpringBootTest
class OperationLifecyclePersistenceTest extends PostgreSqlTestSupport {

  /** The instant the scenario starts at. */
  private static final Instant NOW = Instant.parse("2026-11-27T11:30:00Z");

  /** The protocol answered by the fake of the Central. */
  private static final String PROTOCOL = InMemoryCentralPortabilityPort.PROTOCOL;

  /** The wait the Central asks between two polls. */
  private static final Duration RETRY_AFTER = Duration.ofSeconds(2);

  @Autowired private PortabilityOperationRepository operations;
  @Autowired private OperationEventRepository events;
  @Autowired private TransactionRunner transactions;
  @Autowired private JdbcTemplate jdbc;

  private final MutableClock clock = new MutableClock(NOW);
  private final InMemoryCentralPortabilityPort central = new InMemoryCentralPortabilityPort();
  private final InMemoryCentralStatusPort status = new InMemoryCentralStatusPort();

  @BeforeEach
  void clean() {
    // TRUNCATE does not fire the row trigger of the trail, so the append-only
    // guarantee survives the isolation of the tests.
    jdbc.execute(
        "TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void closesTheOperationAndTheWholeTrailWhenTheCentralConfirmsIt() {
    // The Central accepts the operation: it leaves CREATED and becomes PENDING.
    StartOperation.StartOperationResult started = startOperation().execute(request(), key());
    assertThat(started.operation().status()).isEqualTo(OperationStatus.PENDING);
    assertThat(started.operation().protocol()).isEqualTo(PROTOCOL);
    assertThat(started.replay()).isFalse();

    PortabilityOperation pending = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(pending.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(pending.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
    assertThat(pending.installments()).hasSize(2);
    assertThat(pending.receivingAccount())
        .isEqualTo(new ReceivingAccount("001", "1234", "56789-0"));

    // The Central is still processing: the operation stays PENDING and the next poll is
    // scheduled.
    status.answerWith(new CentralStatus.Pending(RETRY_AFTER, "{\"status\":\"PENDENTE\"}"));
    assertThat(pollOperation(events).pollBatch().polled()).isEqualTo(1);

    PortabilityOperation waiting = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(waiting.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(waiting.nextPollAt()).isEqualTo(NOW.plus(RETRY_AFTER));

    // The Central confirms: the operation is confirmed, the account is updated and the
    // operation is completed, in one transaction.
    clock.advance(RETRY_AFTER.plusSeconds(1));
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));
    assertThat(pollOperation(events).pollBatch().polled()).isEqualTo(1);

    PortabilityOperation completed = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(completed.status()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(completed.protocol()).isEqualTo(PROTOCOL);
    assertThat(completed.rejectionReason()).isNull();
    assertThat(completed.accountUpdatedAt()).isEqualTo(clock.instant());
    assertThat(completed.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
    assertThat(completed.installments()).hasSize(2);

    // The trail proves the whole path, in order.
    List<OperationEvent> trail = events.findByOperationId(completed.id());
    assertThat(trail).extracting(OperationEvent::eventType)
        .containsExactly(
            EventType.OPERATION_CREATED,
            EventType.OPERATION_SUBMITTED,
            EventType.POLL_ATTEMPT,
            EventType.OPERATION_CONFIRMED,
            EventType.ACCOUNT_UPDATED,
            EventType.OPERATION_COMPLETED);
    assertThat(trail.get(0).previousStatus()).isNull();
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CREATED);
    assertThat(trail.get(1).previousStatus()).isEqualTo(OperationStatus.CREATED);
    assertThat(trail.get(1).newStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(2).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(2).newStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(3).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(3).newStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(3).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
    assertThat(trail.get(4).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(4).newStatus()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(trail.get(5).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(5).newStatus()).isEqualTo(OperationStatus.COMPLETED);
  }

  @Test
  void closesTheOperationWithTheReasonWhenTheCentralRejectsIt() {
    StartOperation.StartOperationResult started = startOperation().execute(request(), key());
    assertThat(started.operation().status()).isEqualTo(OperationStatus.PENDING);

    status.answerWith(
        new CentralStatus.Rejected(
            new RejectionReason.MarginInsufficient(), "{\"status\":\"REJEITADA\"}"));
    assertThat(pollOperation(events).pollBatch().polled()).isEqualTo(1);

    PortabilityOperation rejected = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(rejected.status()).isEqualTo(OperationStatus.REJECTED);
    assertThat(rejected.rejectionReason()).isEqualTo(new RejectionReason.MarginInsufficient());
    assertThat(rejected.rejectionReason().code()).isEqualTo("MARGIN_INSUFFICIENT");
    assertThat(rejected.accountUpdatedAt()).isNull();

    List<OperationEvent> trail = events.findByOperationId(rejected.id());
    assertThat(trail).extracting(OperationEvent::eventType)
        .containsExactly(
            EventType.OPERATION_CREATED,
            EventType.OPERATION_SUBMITTED,
            EventType.OPERATION_REJECTED);
    assertThat(trail.get(2).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(2).newStatus()).isEqualTo(OperationStatus.REJECTED);
    assertThat(trail.get(2).rawPayload()).isEqualTo("{\"status\":\"REJEITADA\"}");
  }

  @Test
  void rollsBackTheClosureWhenTheTrailFailsBetweenTheSteps() {
    startOperation().execute(request(), key());

    // The confirmation writes three events; the second one fails: the transaction must
    // undo the state of the operation and the event that was already written.
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));
    PollOperation worker =
        pollOperation(new FailingOperationEventRepository(events, 2));

    assertThatThrownBy(worker::pollBatch)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("the trail failed between the steps");

    PortabilityOperation pending = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(pending.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(pending.accountUpdatedAt()).isNull();
    assertThat(pending.rejectionReason()).isNull();
    assertThat(events.findByOperationId(pending.id()))
        .extracting(OperationEvent::eventType)
        .containsExactly(EventType.OPERATION_CREATED, EventType.OPERATION_SUBMITTED);
  }

  private StartOperation startOperation() {
    return new StartOperation(
        operations,
        InMemoryAgendaGateway.answering(agenda()),
        central,
        events,
        new OperationPolicy(),
        transactions,
        clock,
        Duration.ofSeconds(60));
  }

  private PollOperation pollOperation(OperationEventRepository trail) {
    TransitionRecorder recorder = new TransitionRecorder(operations, trail, transactions);
    return new PollOperation(
        operations,
        central,
        status,
        new CompleteOperation(recorder, clock),
        recorder,
        clock,
        new IntegrationBackoff(Duration.ofSeconds(2), Duration.ofSeconds(60)),
        20);
  }

  private static OperationRequest request() {
    return new OperationRequest(
        new BeneficiaryId("BEN-001"),
        new AgreementId("AGR-001"),
        List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
        "ConsigPort",
        new ReceivingAccount("001", "1234", "56789-0"));
  }

  private static BeneficiaryAgenda agenda() {
    return new BeneficiaryAgenda(
        new BeneficiaryId("BEN-001"),
        List.of(
            new Installment(
                new InstallmentId("PAR-001"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05"),
                InstallmentStatus.OPEN),
            new Installment(
                new InstallmentId("PAR-002"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"),
                InstallmentStatus.OPEN)));
  }

  private static IdempotencyKey key() {
    return new IdempotencyKey(
        UUID.nameUUIDFromBytes(
            "key-lifecycle".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }
}
