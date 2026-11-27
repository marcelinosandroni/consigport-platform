package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.Page;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of {@link PortabilityOperationRepository}.
 *
 * <p>A hand-written fake: it keeps the operations in a list and refuses a second
 * operation with the same idempotency key, like the unique constraint of the database
 * does. Like the storage, it stamps the instant of the last write of every operation.
 */
public final class InMemoryPortabilityOperationRepository
    implements PortabilityOperationRepository {

  private final List<PortabilityOperation> operations = new ArrayList<>();
  private final Map<UUID, Instant> lastWrite = new HashMap<>();
  private final Clock clock;

  /** Builds the fake with the clock of the machine. */
  public InMemoryPortabilityOperationRepository() {
    this(Clock.systemUTC());
  }

  /**
   * Builds the fake with the clock that stamps the writes.
   *
   * @param clock the clock of the fake
   */
  public InMemoryPortabilityOperationRepository(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void save(PortabilityOperation operation) {
    Optional<PortabilityOperation> taken = findByIdempotencyKey(operation.idempotencyKey());
    if (taken.isPresent() && !taken.get().id().equals(operation.id())) {
      throw new IdempotencyKeyAlreadyUsedException(operation.idempotencyKey());
    }
    operations.removeIf(stored -> stored.id().equals(operation.id()));
    operations.add(operation);
    lastWrite.put(operation.id().value(), Instant.now(clock));
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

  @Override
  public List<PortabilityOperation> findDueForPolling(Instant now, int limit) {
    return operations.stream()
        .filter(operation -> isDue(operation, now))
        .limit(limit)
        .toList();
  }

  @Override
  public Page<OperationSummary> list(OperationFilter filter, int page, int size) {
    List<PortabilityOperation> filtered =
        operations.stream()
            .filter(operation -> matches(operation, filter))
            .sorted(
                Comparator.comparing(PortabilityOperation::createdAt)
                    .thenComparing(operation -> operation.id().value()))
            .toList();
    int from = Math.min(page * size, filtered.size());
    int to = Math.min(from + size, filtered.size());
    return new Page<>(
        filtered.subList(from, to).stream().map(this::toSummary).toList(),
        page,
        size,
        filtered.size());
  }

  @Override
  public Optional<OperationDetail> findDetailById(PortabilityOperation.OperationId id) {
    return findById(id).map(this::toDetail);
  }

  private OperationSummary toSummary(PortabilityOperation operation) {
    return new OperationSummary(
        operation.id(),
        operation.protocol(),
        operation.beneficiaryId(),
        operation.status(),
        operation.totalAmount(),
        operation.createdAt(),
        updatedAtOf(operation));
  }

  private OperationDetail toDetail(PortabilityOperation operation) {
    return new OperationDetail(
        operation.id(),
        operation.protocol(),
        operation.beneficiaryId(),
        operation.agreementId(),
        operation.installments(),
        operation.assigneeCreditor(),
        operation.receivingAccount(),
        operation.totalAmount(),
        operation.status(),
        operation.rejectionReason(),
        operation.retryCount(),
        operation.accountUpdatedAt(),
        operation.createdAt(),
        updatedAtOf(operation));
  }

  private Instant updatedAtOf(PortabilityOperation operation) {
    return lastWrite.getOrDefault(operation.id().value(), operation.createdAt());
  }

  private static boolean matches(PortabilityOperation operation, OperationFilter filter) {
    return filter instanceof OperationFilter.WithStatus withStatus
        ? operation.status() == withStatus.status()
        : true;
  }

  private static boolean isDue(PortabilityOperation operation, Instant now) {
    if (operation.status() != OperationStatus.PENDING
        && operation.status() != OperationStatus.INTEGRATION_ERROR) {
      return false;
    }
    return operation.nextPollAt() == null || !operation.nextPollAt().isAfter(now);
  }
}
