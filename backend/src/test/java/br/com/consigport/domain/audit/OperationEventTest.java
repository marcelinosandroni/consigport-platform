package br.com.consigport.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the audit event.
 *
 * <p>The event is immutable and append-only: the test proves that the class exposes no
 * public mutator and that every field is final.
 */
class OperationEventTest {

  private static final PortabilityOperation.OperationId OPERATION_ID =
      new PortabilityOperation.OperationId(
          UUID.fromString("c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601"));

  private static final Instant CREATED_AT = Instant.parse("2026-11-23T12:00:05Z");

  @Test
  void recordsTheChangeOfTheOperation() {
    OperationEvent event =
        OperationEvent.record(
            OPERATION_ID,
            OperationStatus.CREATED,
            OperationStatus.PENDING,
            EventType.OPERATION_SUBMITTED,
            "Operation submitted to the Central",
            "{\"protocolo\":\"PRT-20261126-0001\"}",
            CREATED_AT);

    assertThat(event.eventId()).isNull();
    assertThat(event.operationId()).isEqualTo(OPERATION_ID);
    assertThat(event.previousStatus()).isEqualTo(OperationStatus.CREATED);
    assertThat(event.newStatus()).isEqualTo(OperationStatus.PENDING);
    assertThat(event.eventType()).isEqualTo(EventType.OPERATION_SUBMITTED);
    assertThat(event.details()).isEqualTo("Operation submitted to the Central");
    assertThat(event.rawPayload()).isEqualTo("{\"protocolo\":\"PRT-20261126-0001\"}");
    assertThat(event.createdAt()).isEqualTo(CREATED_AT);
  }

  @Test
  void acceptsTheCreationEventWithoutPreviousStatus() {
    OperationEvent event =
        OperationEvent.record(
            OPERATION_ID,
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created with 2 installments",
            null,
            CREATED_AT);

    assertThat(event.previousStatus()).isNull();
    assertThat(event.rawPayload()).isNull();
  }

  @Test
  void comparesByValue() {
    OperationEvent first = creationEvent();
    OperationEvent second = creationEvent();

    assertThat(first).isEqualTo(second);
  }

  @Test
  void refusesAMissingOperation() {
    assertThatThrownBy(
            () ->
                OperationEvent.record(
                    null,
                    null,
                    OperationStatus.CREATED,
                    EventType.OPERATION_CREATED,
                    "Operation created",
                    null,
                    CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("operationId must not be null");
  }

  @Test
  void refusesAMissingNewStatus() {
    assertThatThrownBy(
            () ->
                OperationEvent.record(
                    OPERATION_ID,
                    null,
                    null,
                    EventType.OPERATION_CREATED,
                    "Operation created",
                    null,
                    CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("newStatus must not be null");
  }

  @Test
  void refusesAMissingEventType() {
    assertThatThrownBy(
            () ->
                OperationEvent.record(
                    OPERATION_ID,
                    null,
                    OperationStatus.CREATED,
                    null,
                    "Operation created",
                    null,
                    CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("eventType must not be null");
  }

  @Test
  void refusesBlankDetails() {
    assertThatThrownBy(
            () ->
                OperationEvent.record(
                    OPERATION_ID,
                    null,
                    OperationStatus.CREATED,
                    EventType.OPERATION_CREATED,
                    "  ",
                    null,
                    CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("details must not be blank");
  }

  @Test
  void refusesAMissingInstant() {
    assertThatThrownBy(
            () ->
                OperationEvent.record(
                    OPERATION_ID,
                    null,
                    OperationStatus.CREATED,
                    EventType.OPERATION_CREATED,
                    "Operation created",
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("createdAt must not be null");
  }

  @Test
  void refusesAnIdentifierThatIsNotPositive() {
    assertThatThrownBy(
            () ->
                new OperationEvent(
                    0L,
                    OPERATION_ID,
                    null,
                    OperationStatus.CREATED,
                    EventType.OPERATION_CREATED,
                    "Operation created",
                    null,
                    CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("eventId must be positive");
  }

  @Test
  void exposesNoPublicMutator() {
    Set<String> publicMethods =
        Arrays.stream(OperationEvent.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertThat(publicMethods)
        .containsExactlyInAnyOrder(
            "eventId",
            "operationId",
            "previousStatus",
            "newStatus",
            "eventType",
            "details",
            "rawPayload",
            "createdAt",
            "record",
            "equals",
            "hashCode",
            "toString");
  }

  @Test
  void keepsEveryFieldFinal() {
    assertThat(OperationEvent.class.getDeclaredFields())
        .filteredOn(field -> !Modifier.isStatic(field.getModifiers()))
        .allSatisfy(
            field ->
                assertThat(Modifier.isFinal(field.getModifiers()))
                    .as("field %s must be final", field.getName())
                    .isTrue());
  }

  @Test
  void declaresNoPublicField() {
    assertThat(
            Arrays.stream(OperationEvent.class.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers())))
        .isEmpty();
  }

  private static OperationEvent creationEvent() {
    return OperationEvent.record(
        OPERATION_ID,
        null,
        OperationStatus.CREATED,
        EventType.OPERATION_CREATED,
        "Operation created",
        null,
        CREATED_AT);
  }
}
