package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.portability.OperationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Answer of the consultation of the trail of the audit of one operation.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The trail is read in chronological order.
 *
 * @param operationId the identity of the operation
 * @param events the events of the trail, from the oldest to the newest
 */
public record OperationEventsResponse(String operationId, List<OperationEventResponse> events) {

  /**
   * Answer of one event of the trail.
   *
   * @param eventId the sequential identifier of the event
   * @param previousStatus the status before the change, {@code null} on the creation
   * @param newStatus the status after the change
   * @param eventType the type of the event
   * @param details the readable description of the event
   * @param rawPayload the payload of the Central, when there is one
   * @param createdAt the instant of the event
   */
  public record OperationEventResponse(
      Long eventId,
      OperationStatus previousStatus,
      OperationStatus newStatus,
      EventType eventType,
      String details,
      Map<String, Object> rawPayload,
      Instant createdAt) {}
}
