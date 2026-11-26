package br.com.consigport.domain.audit;

import br.com.consigport.domain.portability.PortabilityOperation;
import java.util.List;

/**
 * Port of the append-only audit trail.
 *
 * <p>The trail only grows: the port exposes the append and the queries, never an update
 * and never a delete. The storage rejects any change with a trigger.
 */
public interface OperationEventRepository {

  /**
   * Appends an event to the trail.
   *
   * @param event the event to append
   * @return the stored event, with the identifier assigned by the storage
   */
  OperationEvent append(OperationEvent event);

  /**
   * Lists the events of an operation in chronological order.
   *
   * @param operationId the operation whose trail is read
   * @return the events, from the oldest to the newest
   */
  List<OperationEvent> findByOperationId(PortabilityOperation.OperationId operationId);
}
