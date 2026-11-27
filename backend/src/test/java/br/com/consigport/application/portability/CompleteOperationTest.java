package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.InMemoryOperationEventRepository;
import br.com.consigport.domain.audit.OperationEvent;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the closure of a portability operation when the Central answers
 * terminally.
 *
 * <p>A confirmation closes the cycle in one transaction: the status becomes
 * {@code CONFIRMED}, the receiving account is updated and the operation becomes
 * {@code COMPLETED}. The trail proves the whole path. A rejection closes the operation
 * with the reason of the Central.
 */
class CompleteOperationTest {

  private static final Instant NOW = Instant.parse("2026-11-27T11:30:00Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryOperationEventRepository events;
  private AtomicInteger transactions;
  private CompleteOperation useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    events = new InMemoryOperationEventRepository();
    transactions = new AtomicInteger();
    TransitionRecorder recorder =
        new TransitionRecorder(
            operations,
            events,
            work -> {
              transactions.incrementAndGet();
              work.run();
            });
    useCase = new CompleteOperation(recorder, new MutableClock(NOW));
  }

  @Test
  void closesTheOperationWhenTheCentralConfirmsIt() {
    operations.save(pending());

    useCase.confirm(pending(), "{\"status\":\"CONFIRMADA\"}");

    PortabilityOperation stored = stored();
    assertThat(stored.status()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(stored.accountUpdatedAt()).isEqualTo(NOW);
  }

  @Test
  void provesTheWholePathInTheTrail() {
    operations.save(pending());

    useCase.confirm(pending(), "{\"status\":\"CONFIRMADA\"}");

    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail).hasSize(3);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CONFIRMED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.ACCOUNT_UPDATED);
    assertThat(trail.get(1).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(1).newStatus()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(trail.get(2).eventType()).isEqualTo(EventType.OPERATION_COMPLETED);
    assertThat(trail.get(2).previousStatus()).isEqualTo(OperationStatus.CONFIRMED);
    assertThat(trail.get(2).newStatus()).isEqualTo(OperationStatus.COMPLETED);
  }

  @Test
  void writesTheWholeClosureInOneTransaction() {
    operations.save(pending());

    useCase.confirm(pending(), "{\"status\":\"CONFIRMADA\"}");

    assertThat(transactions.get()).isEqualTo(1);
  }

  @Test
  void keepsTheRawAnswerOfTheCentralInTheConfirmationEventOnly() {
    operations.save(pending());

    useCase.confirm(pending(), "{\"status\":\"CONFIRMADA\"}");

    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail.get(0).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
    assertThat(trail.get(1).rawPayload()).isNull();
    assertThat(trail.get(2).rawPayload()).isNull();
  }

  @Test
  void closesTheOperationWhenTheCentralRejectsIt() {
    operations.save(pending());

    useCase.reject(pending(), new RejectionReason.MarginInsufficient(), "{\"status\":\"REJEITADA\"}");

    PortabilityOperation stored = stored();
    assertThat(stored.status()).isEqualTo(OperationStatus.REJECTED);
    assertThat(stored.rejectionReason()).isEqualTo(new RejectionReason.MarginInsufficient());
    assertThat(stored.accountUpdatedAt()).isNull();

    List<OperationEvent> trail = events.findByOperationId(pending().id());
    assertThat(trail).hasSize(1);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_REJECTED);
    assertThat(trail.get(0).previousStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(trail.get(0).newStatus()).isEqualTo(OperationStatus.REJECTED);
    assertThat(trail.get(0).details()).contains("MARGIN_INSUFFICIENT");
    assertThat(trail.get(0).rawPayload()).isEqualTo("{\"status\":\"REJEITADA\"}");
    assertThat(transactions.get()).isEqualTo(1);
  }

  @Test
  void refusesToCloseAnOperationThatTheCentralNeverAccepted() {
    operations.save(created());

    assertThatThrownBy(() -> useCase.confirm(created(), "{\"status\":\"CONFIRMADA\"}"))
        .isInstanceOf(PortabilityOperation.InvalidTransitionException.class)
        .hasMessage("operation cannot move from CREATED to CONFIRMED");

    assertThat(transactions.get()).isZero();
  }

  @Test
  void refusesToCloseAnOperationThatIsAlreadyClosed() {
    operations.save(pending().confirm().complete(NOW));

    assertThatThrownBy(() -> useCase.confirm(pending().confirm().complete(NOW), "{}"))
        .isInstanceOf(PortabilityOperation.InvalidTransitionException.class)
        .hasMessage("operation cannot move from COMPLETED to CONFIRMED");

    assertThat(transactions.get()).isZero();
  }

  private PortabilityOperation stored() {
    return operations.findById(pending().id()).orElseThrow();
  }

  private static PortabilityOperation pending() {
    return created().submit(PROTOCOL);
  }

  private static PortabilityOperation created() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(
            UUID.nameUUIDFromBytes("operation-2.13".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
        NOW);
  }
}
