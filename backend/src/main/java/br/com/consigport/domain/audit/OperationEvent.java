package br.com.consigport.domain.audit;

import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import java.time.Instant;

/**
 * One entry of the append-only audit trail of a portability operation.
 *
 * <p>The event is immutable: it has no mutators and its fields are final. It is never
 * updated and never removed; the database rejects any change with a trigger. The
 * sequential {@code eventId} is assigned by the storage, so it is {@code null} until the
 * event is appended.
 *
 * @param eventId sequential identifier assigned by the storage, {@code null} before the
 *     append
 * @param operationId the operation the event belongs to
 * @param previousStatus the status before the change, {@code null} on the creation
 * @param newStatus the status after the change
 * @param eventType the type of the event
 * @param details a readable description of the event
 * @param rawPayload the raw payload of the Central, when there is one
 * @param createdAt the instant of the event
 */
public record OperationEvent(
    Long eventId,
    PortabilityOperation.OperationId operationId,
    OperationStatus previousStatus,
    OperationStatus newStatus,
    EventType eventType,
    String details,
    String rawPayload,
    Instant createdAt) {

  /**
   * Validates the event.
   *
   * @param eventId sequential identifier assigned by the storage, {@code null} before the
   *     append
   * @param operationId the operation the event belongs to
   * @param previousStatus the status before the change, {@code null} on the creation
   * @param newStatus the status after the change
   * @param eventType the type of the event
   * @param details a readable description of the event
   * @param rawPayload the raw payload of the Central, when there is one
   * @param createdAt the instant of the event
   * @throws IllegalArgumentException when any invariant is broken
   */
  public OperationEvent {
    if (eventId != null && eventId < 1) {
      throw new IllegalArgumentException("eventId must be positive");
    }
    if (operationId == null) {
      throw new IllegalArgumentException("operationId must not be null");
    }
    if (newStatus == null) {
      throw new IllegalArgumentException("newStatus must not be null");
    }
    if (eventType == null) {
      throw new IllegalArgumentException("eventType must not be null");
    }
    if (details == null || details.isBlank()) {
      throw new IllegalArgumentException("details must not be blank");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
  }

  /**
   * Records an event that is not stored yet.
   *
   * @param operationId the operation the event belongs to
   * @param previousStatus the status before the change, {@code null} on the creation
   * @param newStatus the status after the change
   * @param eventType the type of the event
   * @param details a readable description of the event
   * @param rawPayload the raw payload of the Central, when there is one
   * @param createdAt the instant of the event
   * @return the event, without the identifier assigned by the storage
   */
  public static OperationEvent record(
      PortabilityOperation.OperationId operationId,
      OperationStatus previousStatus,
      OperationStatus newStatus,
      EventType eventType,
      String details,
      String rawPayload,
      Instant createdAt) {
    return new OperationEvent(
        null, operationId, previousStatus, newStatus, eventType, details, rawPayload, createdAt);
  }
}
