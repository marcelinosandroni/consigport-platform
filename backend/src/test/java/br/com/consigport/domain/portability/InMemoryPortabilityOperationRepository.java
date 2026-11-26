package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.IdempotencyKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory implementation of {@link PortabilityOperationRepository}.
 *
 * <p>A hand-written fake: it keeps the operations in a list and refuses a second
 * operation with the same idempotency key, like the unique constraint of the database
 * does.
 */
public final class InMemoryPortabilityOperationRepository
    implements PortabilityOperationRepository {

  private final List<PortabilityOperation> operations = new ArrayList<>();

  @Override
  public void save(PortabilityOperation operation) {
    if (findByIdempotencyKey(operation.idempotencyKey()).isPresent()) {
      throw new IdempotencyKeyAlreadyUsedException(operation.idempotencyKey());
    }
    operations.removeIf(stored -> stored.id().equals(operation.id()));
    operations.add(operation);
  }

  @Override
  public Optional<PortabilityOperation> findById(PortabilityOperation.OperationId id) {
    return operations.stream().filter(operation -> operation.id().equals(id)).findFirst();
  }

  @Override
  public Optional<PortabilityOperation> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
    return operations.stream()
        .filter(operation -> operation.idempotencyKey().equals(idempotencyKey))
        .findFirst();
  }

  @Override
  public Optional<PortabilityOperation> findByProtocol(String protocol) {
    return operations.stream()
        .filter(operation -> protocol.equals(operation.protocol()))
        .findFirst();
  }

  /**
   * How many operations are stored.
   *
   * @return the size of the storage
   */
  public int size() {
    return operations.size();
  }
}
