package br.com.consigport.domain.portability;

/**
 * Lifecycle of a portability operation (ARCHITECTURE.md §7).
 *
 * <p>{@code COMPLETED} and {@code REJECTED} are terminal: no transition leaves them.
 * {@code INTEGRATION_ERROR} is resumable and never duplicates the submission.
 */
public enum OperationStatus {

  /** Created locally, not yet accepted by the Central. */
  CREATED,

  /** Accepted by the Central, waiting for the terminal poll. */
  PENDING,

  /** The Central confirmed the operation. */
  CONFIRMED,

  /** The receiving account was updated: the operation is closed. */
  COMPLETED,

  /** The Central rejected the operation: the reason is required. */
  REJECTED,

  /** The integration failed after the retries; the operation can be resumed. */
  INTEGRATION_ERROR;

  /**
   * Whether the operation reached a terminal state.
   *
   * @return {@code true} for {@code COMPLETED} and {@code REJECTED}
   */
  public boolean isTerminal() {
    return this == COMPLETED || this == REJECTED;
  }
}
