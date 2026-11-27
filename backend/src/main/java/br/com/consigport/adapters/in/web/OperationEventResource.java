package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.OperationEventsResponse;
import br.com.consigport.adapters.in.web.dto.OperationEventsResponse.OperationEventResponse;
import br.com.consigport.application.audit.ListOperationEvents;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.shared.DomainError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public edge of the trail of the audit of one operation.
 *
 * <p>The trail is append-only and read in chronological order: it is the proof of what
 * happened with the operation, from the creation to the terminal state.
 *
 * <p>The payload of the Central is exposed as an object: when the Central answered a
 * JSON object, the object itself travels in the answer; when it answered another body,
 * the text travels under the key {@code body}, so the evidence is never lost.
 */
@RestController
@RequestMapping("/api/v1/portability-operations")
public class OperationEventResource {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final ListOperationEvents listOperationEvents;

  /**
   * Builds the resource.
   *
   * @param listOperationEvents the use case that reads the trail
   */
  public OperationEventResource(ListOperationEvents listOperationEvents) {
    this.listOperationEvents = listOperationEvents;
  }

  /**
   * Lists the events of the operation.
   *
   * @param operationId the identity of the operation
   * @return the trail of the operation, from the oldest to the newest
   * @throws DomainError.OperationNotFound when the operation does not exist
   */
  @GetMapping("/{operationId}/events")
  public OperationEventsResponse events(@PathVariable String operationId) {
    PortabilityOperation.OperationId id = PortabilityOperation.OperationId.parse(operationId);
    List<OperationEvent> trail = listOperationEvents.execute(id);

    return new OperationEventsResponse(
        id.value().toString(),
        trail.stream().map(OperationEventResource::toResponse).toList());
  }

  private static OperationEventResponse toResponse(OperationEvent event) {
    return new OperationEventResponse(
        event.eventId(),
        event.previousStatus(),
        event.newStatus(),
        event.eventType(),
        event.details(),
        toRawPayload(event.rawPayload()),
        event.createdAt());
  }

  private static Map<String, Object> toRawPayload(String rawPayload) {
    if (rawPayload == null) {
      return null;
    }
    try {
      return MAPPER.readValue(rawPayload, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException exception) {
      // The Central answered a body that is not a JSON object: the evidence travels as
      // it was received, under the key "body".
      return Map.of("body", rawPayload);
    }
  }
}
