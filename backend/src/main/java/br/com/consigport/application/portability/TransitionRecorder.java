package br.com.consigport.application.portability;

import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.TransactionRunner;
import java.util.List;

/**
 * Writes a transition of an operation and the events that prove it, in one transaction.
 *
 * <p>The state of the operation and the trail of the audit are written together: a
 * transition that is not proven is not written (BR-12). When any step fails, the
 * transaction rolls back and the operation keeps the state it had.
 *
 * <p>When another worker changed the operation in the meantime, the stored operation is
 * the truth and this attempt is discarded: the optimistic lock of the storage decides who
 * moves the operation forward.
 */
public final class TransitionRecorder {

  private final PortabilityOperationRepository operations;
  private final OperationEventRepository events;
  private final TransactionRunner transactions;

  /**
   * Builds the recorder.
   *
   * @param operations the storage of the operations
   * @param events the append-only trail of the audit
   * @param transactions the boundary of the transaction of each transition
   */
  public TransitionRecorder(
      PortabilityOperationRepository operations,
      OperationEventRepository events,
      TransactionRunner transactions) {
    this.operations = operations;
    this.events = events;
    this.transactions = transactions;
  }

  /**
   * Records the transition of the operation.
   *
   * @param operation the operation with the new state
   * @param trail the events that prove the transition, in the order they happened
   */
  public void record(PortabilityOperation operation, List<OperationEvent> trail) {
    try {
      transactions.run(
          () -> {
            operations.save(operation);
            trail.forEach(events::append);
          });
    } catch (ConcurrentOperationUpdateException conflict) {
      // Another worker moved the operation forward: the stored state is the truth.
    }
  }
}
