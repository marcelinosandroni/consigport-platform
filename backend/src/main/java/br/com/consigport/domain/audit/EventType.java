package br.com.consigport.domain.audit;

/**
 * Type of an event of the audit trail.
 *
 * <p>The vocabulary is closed and stable: the same names are persisted and exposed by
 * the public API.
 */
public enum EventType {

  /** The operation was created locally. */
  OPERATION_CREATED,

  /** The Central accepted the operation. */
  OPERATION_SUBMITTED,

  /** The poll returned {@code 204}: the operation is still pending. */
  POLL_ATTEMPT,

  /** The Central confirmed the operation. */
  OPERATION_CONFIRMED,

  /** The Central rejected the operation. */
  OPERATION_REJECTED,

  /** The receiving account was updated. */
  ACCOUNT_UPDATED,

  /** The operation was closed. */
  OPERATION_COMPLETED,

  /** The integration failed after the retries. */
  INTEGRATION_FAILED
}
