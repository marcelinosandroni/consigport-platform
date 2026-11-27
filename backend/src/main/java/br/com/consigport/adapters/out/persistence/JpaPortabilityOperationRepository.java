package br.com.consigport.adapters.out.persistence;

import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.portability.RejectionReason;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
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
