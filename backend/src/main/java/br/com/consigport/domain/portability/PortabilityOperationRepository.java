package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.IdempotencyKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port of the storage of the portability operations.
 *
 * <p>Declared in the domain and implemented by the persistence adapter. The idempotency
 * key is unique (BR-07): the same key never creates a second operation.
 */
public interface PortabilityOperationRepository {

  /**
   * Stores the operation.
   *
   * <p>The idempotency key is unique (BR-07): the same key never creates a second
   * operation.
   *
   * @param operation the operation to store
   * @throws IdempotencyKeyAlreadyUsedException when another operation already holds the
   *     idempotency key
   */
  void save(PortabilityOperation operation);

  /**
   * Finds an operation by its identity.
   *
   * @param id the identity of the operation
   * @return the operation, or empty when it does not exist
   */
  Optional<PortabilityOperation> findById(PortabilityOperation.OperationId id);

  /**
   * Finds the operation of an idempotency key.
   *
   * @param idempotencyKey the key that identifies the request
   * @return the operation, or empty when the key was never used
   */
  Optional<PortabilityOperation> findByIdempotencyKey(IdempotencyKey idempotencyKey);

  /**
   * Finds the operation of a protocol.
   *
   * @param protocol the protocol returned by the Central
   * @return the operation, or empty when the protocol is unknown
   */
  Optional<PortabilityOperation> findByProtocol(String protocol);

  /**
   * Finds the operations whose polling is due.
   *
   * <p>The worker polls the operations that the Central accepted and the ones whose
   * integration failed: both are waiting for the Central. An operation that was never
   * polled has no scheduled poll, so it is due at once (ADR {@code 0003}).
   *
   * @param now the instant of the worker
   * @param limit the maximum number of operations returned
   * @return the operations due for polling, from the oldest to the newest
   */
  List<PortabilityOperation> findDueForPolling(Instant now, int limit);
}
