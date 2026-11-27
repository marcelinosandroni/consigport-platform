package br.com.consigport.adapters.out.persistence;

import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationDetail;
import br.com.consigport.domain.portability.OperationFilter;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.OperationSummary;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.portability.RejectionReason;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.Page;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter of the {@link PortabilityOperationRepository} port over PostgreSQL.
 *
 * <p>The mapping is explicit, field by field. The aggregate is immutable, so the
 * adapter loads the row, applies the new state and lets the dirty checking of the
 * persistence context write it.
 */
@Repository
public class JpaPortabilityOperationRepository implements PortabilityOperationRepository {

  /** The unique constraint of the idempotency key, as PostgreSQL names it. */
  private static final String IDEMPOTENCY_KEY_CONSTRAINT =
      "uk_portability_operations_idempotency_key";

  private final EntityManager entityManager;
  private final Clock clock;

  /**
   * Builds the adapter.
   *
   * @param entityManager the entity manager of the persistence context
   * @param clock the clock that stamps the moment of the update
   */
  public JpaPortabilityOperationRepository(EntityManager entityManager, Clock clock) {
    this.entityManager = entityManager;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void save(PortabilityOperation operation) {
    try {
      saveRow(operation);
      // The flush is inside the guard: the unique constraint of the idempotency key
      // is checked by PostgreSQL, and only the flush reaches the database.
      entityManager.flush();
    } catch (PersistenceException exception) {
      if (UniqueViolations.isUniqueViolationOn(
          exception, IDEMPOTENCY_KEY_CONSTRAINT)) {
        throw new IdempotencyKeyAlreadyUsedException(operation.idempotencyKey());
      }
      if (isOptimisticLockFailure(exception)) {
        throw new ConcurrentOperationUpdateException(operation.id());
      }
      throw exception;
    }
  }

  /**
   * Tells whether the database refused the write because another transaction changed the
   * row in the meantime.
   *
   * <p>The optimistic lock is the guard of the concurrent updates of the same operation.
   *
   * @param exception the failure of the persistence provider
   * @return {@code true} when the write lost the optimistic lock
   */
  private static boolean isOptimisticLockFailure(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof OptimisticLockException) {
        return true;
      }
      if (cause.getCause() == cause) {
        return false;
      }
    }
    return false;
  }

  private void saveRow(PortabilityOperation operation) {
    PortabilityOperationJpaEntity entity =
        entityManager.find(PortabilityOperationJpaEntity.class, operation.id().value());
    if (entity == null) {
      entity = new PortabilityOperationJpaEntity();
      entity.setId(operation.id().value());
      entity.setIdempotencyKey(operation.idempotencyKey().value().toString());
      entity.setBeneficiaryId(operation.beneficiaryId().value());
      entity.setInstallments(toRows(operation.installments()));
      entity.setAssigneeCreditor(operation.assigneeCreditor());
      entity.setReceivingBank(operation.receivingAccount().bank());
      entity.setReceivingBranch(operation.receivingAccount().branch());
      entity.setReceivingAccount(operation.receivingAccount().account());
      entity.setTotalAmount(operation.totalAmount().amount());
      entity.setCreatedAt(operation.createdAt());
    }
    entity.setProtocol(operation.protocol());
    entity.setStatus(operation.status().name());
    entity.setRejectionReason(
        operation.rejectionReason() == null ? null : operation.rejectionReason().code());
    entity.setRetryCount(operation.retryCount());
    entity.setNextPollAt(operation.nextPollAt());
    entity.setAccountUpdatedAt(operation.accountUpdatedAt());
    entity.setUpdatedAt(Instant.now(clock));
    if (entityManager.contains(entity)) {
      entityManager.merge(entity);
    } else {
      entityManager.persist(entity);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PortabilityOperation> findById(PortabilityOperation.OperationId id) {
    return Optional.ofNullable(entityManager.find(PortabilityOperationJpaEntity.class, id.value()))
        .map(JpaPortabilityOperationRepository::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PortabilityOperation> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
    return entityManager
        .createQuery(
            "select operation from PortabilityOperationJpaEntity operation"
                + " where operation.idempotencyKey = :idempotencyKey",
            PortabilityOperationJpaEntity.class)
        .setParameter("idempotencyKey", idempotencyKey.value().toString())
        .getResultStream()
        .findFirst()
        .map(JpaPortabilityOperationRepository::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PortabilityOperation> findByProtocol(String protocol) {
    return entityManager
        .createQuery(
            "select operation from PortabilityOperationJpaEntity operation"
                + " where operation.protocol = :protocol",
            PortabilityOperationJpaEntity.class)
        .setParameter("protocol", protocol)
        .getResultStream()
        .findFirst()
        .map(JpaPortabilityOperationRepository::toDomain);
  }

  /**
   * Finds the operations whose polling is due.
   *
   * <p>The worker polls the operations that the Central accepted and the ones whose
   * integration failed. An operation that was never polled has no scheduled poll, so it
   * is due at once.
   *
   * @param now the instant of the worker
   * @param limit the maximum number of operations returned
   * @return the operations due for polling
   */
  @Override
  @Transactional(readOnly = true)
  public List<PortabilityOperation> findDueForPolling(Instant now, int limit) {
    return entityManager
        .createQuery(
            "select operation from PortabilityOperationJpaEntity operation"
                + " where operation.status in :statuses"
                + " and (operation.nextPollAt is null or operation.nextPollAt <= :now)"
                + " order by operation.createdAt",
            PortabilityOperationJpaEntity.class)
        .setParameter("statuses", List.of(OperationStatus.PENDING.name(), OperationStatus.INTEGRATION_ERROR.name()))
        .setParameter("now", now)
        .setMaxResults(limit)
        .getResultStream()
        .map(JpaPortabilityOperationRepository::toDomain)
        .toList();
  }

  /**
   * Lists the operations of a page, from the oldest to the newest.
   *
   * <p>The count and the page are two queries: the count says how many operations the
   * whole result set has, the page says which ones come in it. The identifier breaks the
   * tie of operations created in the same instant, so the order never changes between two
   * pages.
   *
   * @param filter the filter of the consultation
   * @param page the zero-based index of the page
   * @param size the maximum number of operations of the page
   * @return the page of the operations
   */
  @Override
  @Transactional(readOnly = true)
  public Page<OperationSummary> list(OperationFilter filter, int page, int size) {
    if (filter instanceof OperationFilter.WithStatus withStatus) {
      return pageOfStatus(withStatus.status(), page, size);
    }
    return pageOfEveryOperation(page, size);
  }

  private Page<OperationSummary> pageOfStatus(OperationStatus status, int page, int size) {
    long total =
        entityManager
            .createQuery(
                "select count(operation) from PortabilityOperationJpaEntity operation"
                    + " where operation.status = :status",
                Long.class)
            .setParameter("status", status.name())
            .getSingleResult();
    List<OperationSummary> items =
        entityManager
            .createQuery(
                "select operation from PortabilityOperationJpaEntity operation"
                    + " where operation.status = :status"
                    + " order by operation.createdAt, operation.id",
                PortabilityOperationJpaEntity.class)
            .setParameter("status", status.name())
            .setFirstResult(offsetOf(page, size))
            .setMaxResults(size)
            .getResultStream()
            .map(JpaPortabilityOperationRepository::toSummary)
            .toList();
    return new Page<>(items, page, size, total);
  }

  private Page<OperationSummary> pageOfEveryOperation(int page, int size) {
    long total =
        entityManager
            .createQuery(
                "select count(operation) from PortabilityOperationJpaEntity operation", Long.class)
            .getSingleResult();
    List<OperationSummary> items =
        entityManager
            .createQuery(
                "select operation from PortabilityOperationJpaEntity operation"
                    + " order by operation.createdAt, operation.id",
                PortabilityOperationJpaEntity.class)
            .setFirstResult(offsetOf(page, size))
            .setMaxResults(size)
            .getResultStream()
            .map(JpaPortabilityOperationRepository::toSummary)
            .toList();
    return new Page<>(items, page, size, total);
  }

  /**
   * Finds the read model of one operation.
   *
   * @param id the identity of the operation
   * @return the detail of the operation, when the row exists
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<OperationDetail> findDetailById(PortabilityOperation.OperationId id) {
    return Optional.ofNullable(entityManager.find(PortabilityOperationJpaEntity.class, id.value()))
        .map(JpaPortabilityOperationRepository::toDetail);
  }

  /**
   * The offset of the page, without overflowing when the caller asks for a page far
   * beyond the end of the result set.
   */
  private static int offsetOf(int page, int size) {
    return (int) Math.min((long) page * size, Integer.MAX_VALUE);
  }

  /**
   * Rebuilds the read model of the list from its row.
   */
  private static OperationSummary toSummary(PortabilityOperationJpaEntity entity) {
    return new OperationSummary(
        new PortabilityOperation.OperationId(entity.getId()),
        entity.getProtocol(),
        new BeneficiaryId(entity.getBeneficiaryId()),
        OperationStatus.valueOf(entity.getStatus()),
        new Money(entity.getTotalAmount()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  /**
   * Rebuilds the read model of the detail from its row.
   *
   * <p>The schema of {@code ARCHITECTURE.md} §6 has no column for the agreement of the
   * operation: the agreement is the one of the installments, which the request selects
   * from a single agreement.
   */
  private static OperationDetail toDetail(PortabilityOperationJpaEntity entity) {
    List<InstallmentSnapshot> installments = toSnapshots(entity.getInstallments());
    return new OperationDetail(
        new PortabilityOperation.OperationId(entity.getId()),
        entity.getProtocol(),
        new BeneficiaryId(entity.getBeneficiaryId()),
        new AgreementId(installments.get(0).agreementId().value()),
        installments,
        entity.getAssigneeCreditor(),
        new ReceivingAccount(
            entity.getReceivingBank(),
            entity.getReceivingBranch(),
            entity.getReceivingAccount()),
        new Money(entity.getTotalAmount()),
        OperationStatus.valueOf(entity.getStatus()),
        entity.getRejectionReason() == null
            ? null
            : RejectionReason.fromCode(entity.getRejectionReason()),
        entity.getRetryCount(),
        entity.getAccountUpdatedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  /**
   * Rebuilds the aggregate from its row.
   *
   * <p>The schema of {@code ARCHITECTURE.md} §6 has no column for the agreement of the
   * operation: the agreement is the one of the installments, which the request selects
   * from a single agreement.
   */
  private static PortabilityOperation toDomain(PortabilityOperationJpaEntity entity) {
    List<InstallmentSnapshot> installments = toSnapshots(entity.getInstallments());
    return new PortabilityOperation(
        new PortabilityOperation.OperationId(entity.getId()),
        IdempotencyKey.parse(entity.getIdempotencyKey()),
        new BeneficiaryId(entity.getBeneficiaryId()),
        new AgreementId(installments.get(0).agreementId().value()),
        installments,
        entity.getAssigneeCreditor(),
        new ReceivingAccount(
            entity.getReceivingBank(),
            entity.getReceivingBranch(),
            entity.getReceivingAccount()),
        new Money(entity.getTotalAmount()),
        OperationStatus.valueOf(entity.getStatus()),
        entity.getProtocol(),
        entity.getRejectionReason() == null
            ? null
            : RejectionReason.fromCode(entity.getRejectionReason()),
        entity.getRetryCount(),
        entity.getNextPollAt(),
        entity.getAccountUpdatedAt(),
        entity.getCreatedAt());
  }

  private static List<InstallmentSnapshotRow> toRows(List<InstallmentSnapshot> installments) {
    return installments.stream()
        .map(
            installment ->
                new InstallmentSnapshotRow(
                    installment.installmentId().value(),
                    installment.agreementId().value(),
                    installment.amount().amount(),
                    installment.discountDate().toString()))
        .toList();
  }

  private static List<InstallmentSnapshot> toSnapshots(List<InstallmentSnapshotRow> rows) {
    return rows.stream()
        .map(
            row ->
                new InstallmentSnapshot(
                    new InstallmentId(row.installmentId()),
                    new AgreementId(row.agreementId()),
                    new Money(row.amount()),
                    LocalDate.parse(row.discountDate())))
        .toList();
  }
}
