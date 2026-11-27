package br.com.consigport.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.application.portability.CompleteOperation;
import br.com.consigport.application.portability.IntegrationBackoff;
import br.com.consigport.application.portability.PollOperation;
import br.com.consigport.application.portability.TransitionRecorder;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.InMemoryCentralPortabilityPort;
import br.com.consigport.domain.portability.InMemoryCentralStatusPort;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.TransactionRunner;
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
 * The worker of the polling over PostgreSQL 16 (the flow of {@code ARCHITECTURE.md} §11).
 *
 * <p>The test runs against the database of the stack: the operations that are due are
 * found by the query, the transition is written with its event in one transaction and
 * the trail of the audit proves what happened.
 */
@SpringBootTest
class PollingPersistenceTest extends PostgreSqlTestSupport {

  private static final Instant NOW = Instant.parse("2026-11-27T10:15:30Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  @Autowired private PortabilityOperationRepository operations;
  @Autowired private OperationEventRepository events;
  @Autowired private TransactionRunner transactions;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    // TRUNCATE does not fire the row trigger of the trail, so the append-only
    // guarantee survives the isolation of the tests.
    jdbc.execute(
        "TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void findsTheOperationWhosePollIsDue() {
    operations.save(pending().pollAttempt(NOW.minusSeconds(1)));

    List<PortabilityOperation> due = operations.findDueForPolling(NOW, 20);

    assertThat(due).hasSize(1);
    assertThat(due.get(0).protocol()).isEqualTo(PROTOCOL);
  }

  @Test
  void ignoresTheOperationWhosePollIsInTheFuture() {
    operations.save(pending().pollAttempt(NOW.plusSeconds(30)));

    assertThat(operations.findDueForPolling(NOW, 20)).isEmpty();
  }

  @Test
  void ignoresTheOperationThatReachedATerminalStatus() {
    operations.save(pending().confirm());

    assertThat(operations.findDueForPolling(NOW, 20)).isEmpty();
  }

  @Test
  void findsTheOperationWhoseIntegrationFailed() {
    operations.save(pending().registerIntegrationFailure(NOW.minusSeconds(1)));

    assertThat(operations.findDueForPolling(NOW, 20)).hasSize(1);
  }

  @Test
  void limitsTheBatchAndReturnsTheOldestFirst() {
    operations.save(pending(NOW.minusSeconds(3), "PRT-20261127-0001"));
    operations.save(pending(NOW.minusSeconds(2), "PRT-20261127-0002"));
    operations.save(pending(NOW.minusSeconds(1), "PRT-20261127-0003"));

    List<PortabilityOperation> due = operations.findDueForPolling(NOW, 2);

    assertThat(due).hasSize(2);
    assertThat(due.get(0).createdAt()).isEqualTo(NOW.minusSeconds(3));
    assertThat(due.get(1).createdAt()).isEqualTo(NOW.minusSeconds(2));
  }

  @Test
  void closesTheOperationWhenTheCentralConfirmsIt() {
    operations.save(pending().pollAttempt(NOW.minusSeconds(1)));
    InMemoryCentralStatusPort status = new InMemoryCentralStatusPort();
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));
    TransitionRecorder recorder = new TransitionRecorder(operations, events, transactions);
    PollOperation worker =
        new PollOperation(
            operations,
            new InMemoryCentralPortabilityPort(),
            status,
            new CompleteOperation(recorder, java.time.Clock.fixed(NOW, java.time.ZoneOffset.UTC)),
            recorder,
            java.time.Clock.fixed(NOW, java.time.ZoneOffset.UTC),
            new IntegrationBackoff(Duration.ofSeconds(2), Duration.ofSeconds(60)),
            20);

    PollOperation.PollResult result = worker.pollBatch();

    assertThat(result.polled()).isEqualTo(1);
    PortabilityOperation stored = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(stored.accountUpdatedAt()).isEqualTo(NOW);
    List<OperationEvent> trail = events.findByOperationId(stored.id());
    assertThat(trail).hasSize(3);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CONFIRMED);
    assertThat(trail.get(0).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.ACCOUNT_UPDATED);
    assertThat(trail.get(2).eventType()).isEqualTo(EventType.OPERATION_COMPLETED);
  }

  @Test
  void keepsTheOperationResumableWhenTheCentralDoesNotAnswer() {
    operations.save(pending().pollAttempt(NOW.minusSeconds(1)));
    InMemoryCentralStatusPort status = new InMemoryCentralStatusPort();
    status.fail();
    TransitionRecorder recorder = new TransitionRecorder(operations, events, transactions);
    PollOperation worker =
        new PollOperation(
            operations,
            new InMemoryCentralPortabilityPort(),
            status,
            new CompleteOperation(recorder, java.time.Clock.fixed(NOW, java.time.ZoneOffset.UTC)),
            recorder,
            java.time.Clock.fixed(NOW, java.time.ZoneOffset.UTC),
            new IntegrationBackoff(Duration.ofSeconds(2), Duration.ofSeconds(60)),
            20);

    worker.pollBatch();

    PortabilityOperation stored = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
    assertThat(stored.retryCount()).isEqualTo(1);
    assertThat(stored.nextPollAt()).isEqualTo(NOW.plusSeconds(2));
    List<OperationEvent> trail = events.findByOperationId(stored.id());
    assertThat(trail).hasSize(1);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.INTEGRATION_FAILED);
  }

  private static PortabilityOperation pending() {
    return pending(NOW, PROTOCOL);
  }

  private static PortabilityOperation pending(Instant createdAt) {
    return pending(createdAt, PROTOCOL);
  }

  private static PortabilityOperation pending(Instant createdAt, String protocol) {
    return PortabilityOperation.create(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            new IdempotencyKey(UUID.randomUUID()),
            new BeneficiaryId("BEN-001"),
            new AgreementId("AGR-001"),
            List.of(
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"),
                    new AgreementId("AGR-001"),
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05"))),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"),
            createdAt)
        .submit(protocol);
  }
}
