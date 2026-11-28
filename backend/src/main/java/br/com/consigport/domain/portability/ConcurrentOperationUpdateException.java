package br.com.consigport.domain.portability;

/**
 * Raised when the storage refuses the write because another transaction changed the
 * operation in the meantime.
 *
 * <p>The operation is guarded by optimistic locking: two requests that carry the same
 * idempotency key may try to move the same operation forward at the same time. The loser
 * of the race reads the stored operation and answers with it, because the stored state
 * is the truth (BR-07).
 */
public final class ConcurrentOperationUpdateException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient PortabilityOperation.OperationId operationId;

  /**
   * Builds the exception for the operation that was changed in the meantime.
   *
   * @param operationId the identity of the operation
   */
  public ConcurrentOperationUpdateException(PortabilityOperation.OperationId operationId) {
    super("operation was updated by another transaction: " + operationId.value());
    this.operationId = operationId;
  }

  /**
   * The operation that was changed in the meantime.
   *
   * @return the identity of the operation
   */
  public PortabilityOperation.OperationId operationId() {
    return operationId;
  }
}
