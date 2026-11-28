package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.IdempotencyKey;

/**
 * Raised when the storage refuses an operation because its idempotency key is already
 * taken (BR-07).
 *
 * <p>The key is unique in the database, so the loser of a race reads the operation of
 * the winner and answers the caller with it. The error is not a conflict of payload: it
 * only says that another request took the key first.
 */
public final class IdempotencyKeyAlreadyUsedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient IdempotencyKey idempotencyKey;

  /**
   * Builds the exception for the taken key.
   *
   * @param idempotencyKey the key that was already taken
   */
  public IdempotencyKeyAlreadyUsedException(IdempotencyKey idempotencyKey) {
    super("idempotency key already used: " + idempotencyKey.value());
    this.idempotencyKey = idempotencyKey;
  }

  /**
   * The key that was already taken.
   *
   * @return the key
   */
  public IdempotencyKey idempotencyKey() {
    return idempotencyKey;
  }
}
