package br.com.consigport.domain.audit;

import br.com.consigport.domain.portability.PortabilityOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory implementation of {@link OperationEventRepository}.
 *
 * <p>A hand-written fake: it assigns the sequential identifier like the database does
 * and refuses to change or remove an event, because the port has no method for it.
 */
public final class InMemoryOperationEventRepository implements OperationEventRepository {

  private final List<OperationEvent> events = new ArrayList<>();

  @Override
  public OperationEvent append(OperationEvent event) {
    OperationEvent stored =
        new OperationEvent(
            (long) events.size() + 1,
            event.operationId(),
            event.previousStatus(),
            event.newStatus(),
            event.eventType(),
            event.details(),
            event.rawPayload(),
            event.createdAt());
    events.add(stored);
    return stored;
  }

  @Override
  public List<OperationEvent> findByOperationId(PortabilityOperation.OperationId operationId) {
    return events.stream()
        .filter(event -> event.operationId().equals(operationId))
        .sorted(Comparator.comparing(OperationEvent::createdAt))
        .toList();
  }
}
