package br.com.consigport.application.audit;

import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.DomainError;
import java.util.List;

/**
 * Lists the trail of the audit of one portability operation.
 *
 * <p>The trail is append-only and read in chronological order: it is the proof of what
 * happened with the operation, from the creation to the terminal state.
 */
public final class ListOperationEvents {

  private final PortabilityOperationRepository operations;
  private final OperationEventRepository events;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   * @param events the append-only trail of the audit
   */
  public ListOperationEvents(
      PortabilityOperationRepository operations, OperationEventRepository events) {
    this.operations = operations;
    this.events = events;
  }

  /**
   * Lists the events of the operation.
   *
   * @param id the identity of the operation
   * @return the events, from the oldest to the newest
   * @throws DomainError.OperationNotFound when the operation does not exist
   */
  public List<OperationEvent> execute(PortabilityOperation.OperationId id) {
    if (operations.findById(id).isEmpty()) {
      throw new DomainError.OperationNotFound(id.value());
    }
    return events.findByOperationId(id);
  }
}
