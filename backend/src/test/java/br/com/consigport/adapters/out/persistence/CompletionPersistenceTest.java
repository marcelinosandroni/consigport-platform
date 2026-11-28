package br.com.consigport.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.application.portability.CompleteOperation;
import br.com.consigport.application.portability.TransitionRecorder;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
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
import br.com.consigport.support.PostgreSqlTestSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The closure of a portability operation over PostgreSQL 16 (the flow of
 * {@code ARCHITECTURE.md} §11).
 *
 * <p>The test runs against the database of the stack: the confirmation writes the final
 * state of the operation and the whole trail in one transaction, and a failure between
 * the steps rolls back everything, so the trail never shows a closure that did not
 * happen.
 */
@SpringBootTest
class CompletionPersistenceTest extends PostgreSqlTestSupport {

  private static final Instant NOW = Instant.parse("2026-11-27T11:30:00Z");
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
  void closesTheOperationAndItsTrailInOneTransaction() {
    operations.save(pending());

    completeOperation(events).confirm(pending(), "{\"status\":\"CONFIRMADA\"}");

    PortabilityOperation stored = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(stored.accountUpdatedAt()).isEqualTo(NOW);
    List<OperationEvent> trail = events.findByOperationId(stored.id());
    assertThat(trail).hasSize(3);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CONFIRMED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(0).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.ACCOUNT_UPDATED);
    assertThat(trail.get(1).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(1).newStatus()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(trail.get(2).eventType()).isEqualTo(EventType.OPERATION_COMPLETED);
    assertThat(trail.get(2).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(2).newStatus()).isEqualTo(OperationStatus.COMPLETED);
  }

  @Test
  void closesTheRejectedOperationWithItsReason() {
    operations.save(pending());

    completeOperation(events)
        .reject(pending(), new RejectionReason.MarginInsufficient(), "{\"status\":\"REJEITADA\"}");

    PortabilityOperation stored = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.REJECTED);
    assertThat(stored.rejectionReason()).isEqualTo(new RejectionReason.MarginInsufficient());
    assertThat(stored.accountUpdatedAt()).isNull();
    List<OperationEvent> trail = events.findByOperationId(stored.id());
    assertThat(trail).hasSize(1);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_REJECTED);
  }

  @Test
  void rollsBackEverythingWhenTheTrailFailsBetweenTheSteps() {
    operations.save(pending());
    // The first event is written, the second one fails: the transaction must undo the
    // state of the operation and the event that was already written.
    OperationEventRepository failing = new FailingOperationEventRepository(events, 2);

    assertThatThrownBy(
            () -> completeOperation(failing).confirm(pending(), "{\"status\":\"CONFIRMADA\"}"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("the trail failed between the steps");

    PortabilityOperation stored = operations.findByProtocol(PROTOCOL).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(stored.accountUpdatedAt()).isNull();
    assertThat(events.findByOperationId(stored.id())).isEmpty();
  }

  private CompleteOperation completeOperation(OperationEventRepository trail) {
    TransitionRecorder recorder = new TransitionRecorder(operations, trail, transactions);
    return new CompleteOperation(recorder, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static PortabilityOperation pending() {
    return PortabilityOperation.create(
            new PortabilityOperation.OperationId(
                UUID.nameUUIDFromBytes(
                    "operation-2.13".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
            new IdempotencyKey(
                UUID.nameUUIDFromBytes("key-2.13".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
            NOW)
        .submit(PROTOCOL);
  }
}
