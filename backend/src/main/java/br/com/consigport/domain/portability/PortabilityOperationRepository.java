package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.IdempotencyKey;
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
   * @param operation the operation to store
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
}
