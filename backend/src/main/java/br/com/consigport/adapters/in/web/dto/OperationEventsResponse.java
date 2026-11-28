package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.portability.OperationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The trail of the audit of one operation, as the public API answers it.
 *
 * @param operationId the identity of the operation
 * @param events the events of the trail, from the oldest to the newest
 */
@Schema(name = "OperationEventsResponse", requiredProperties = {"operationId", "events"})
public record OperationEventsResponse(String operationId, List<OperationEventResponse> events) {

  /**
   * One event of the trail.
   *
   * @param eventId the sequence of the event
   * @param previousStatus the state before the event, null in the creation
   * @param newStatus the state after the event
   * @param eventType the stable type of the event
   * @param details the readable description of the event
   * @param rawPayload the payload of the Central, when there is one
   * @param createdAt the instant of the event
   */
  @Schema(
      name = "OperationEvent",
      requiredProperties = {
        "eventId",
        "previousStatus",
        "newStatus",
        "eventType",
        "details",
        "rawPayload",
        "createdAt"
      })
  public record OperationEventResponse(
      Long eventId,
      OperationStatus previousStatus,
      OperationStatus newStatus,
      EventType eventType,
      String details,
      Map<String, Object> rawPayload,
      Instant createdAt) {}
}
