package br.com.consigport.application.audit;

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
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the consultation of the trail of the audit of one operation.
 *
 * <p>The trail is read in chronological order and proves every step of the operation.
 */
class ListOperationEventsTest {

  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryOperationEventRepository events;
  private ListOperationEvents useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    events = new InMemoryOperationEventRepository();
    useCase = new ListOperationEvents(operations, events);
  }

  @Test
  void listsTheEventsInChronologicalOrder() {
    PortabilityOperation operation = operation();
    operations.save(operation);
    events.append(
        OperationEvent.record(
            operation.id(), null, OperationStatus.CREATED, EventType.OPERATION_CREATED, "created", null, NOW));
    events.append(
        OperationEvent.record(
            operation.id(),
            OperationStatus.CREATED,
            OperationStatus.PENDING,
            EventType.OPERATION_SUBMITTED,
            "submitted",
            null,
            NOW.plusSeconds(1)));
    events.append(
        OperationEvent.record(
            operation.id(),
            OperationStatus.PENDING,
            OperationStatus.CONFIRMED,
            EventType.OPERATION_CONFIRMED,
            "confirmed",
            "{\"status\":\"CONFIRMADA\"}",
            NOW.plusSeconds(2)));

    List<OperationEvent> trail = useCase.execute(operation.id());

    assertThat(trail).hasSize(3);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CREATED);
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.OPERATION_SUBMITTED);
    assertThat(trail.get(2).eventType()).isEqualTo(EventType.OPERATION_CONFIRMED);
    assertThat(trail.get(2).rawPayload()).isEqualTo("{\"status\":\"CONFIRMADA\"}");
  }

  @Test
  void listsAnEmptyTrailWhenTheOperationHasNoEvent() {
    PortabilityOperation operation = operation();
    operations.save(operation);

    assertThat(useCase.execute(operation.id())).isEmpty();
  }

  @Test
  void refusesAnOperationThatDoesNotExist() {
    PortabilityOperation.OperationId unknown =
        new PortabilityOperation.OperationId(UUID.randomUUID());

    assertThatThrownBy(() -> useCase.execute(unknown))
        .isInstanceOf(DomainError.OperationNotFound.class)
        .extracting(exception -> ((DomainError.OperationNotFound) exception).code())
        .isEqualTo("OPERATION_NOT_FOUND");
  }

  private static PortabilityOperation operation() {
    return PortabilityOperation.create(
            new PortabilityOperation.OperationId(
                UUID.nameUUIDFromBytes(
                    "operation-2.14-events".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
            new IdempotencyKey(
                UUID.nameUUIDFromBytes(
                    "key-2.14-events".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
