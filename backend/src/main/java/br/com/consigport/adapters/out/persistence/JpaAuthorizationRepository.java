package br.com.consigport.adapters.out.persistence;

import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.AuthorizationAlreadyExistsException;
import br.com.consigport.domain.authorization.AuthorizationRepository;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter of the {@link AuthorizationRepository} port over PostgreSQL.
 *
 * <p>The adapter translates the unique constraint of the pair
 * {@code (beneficiaryId, agreementId)} into the domain error, so the application layer
 * never sees a storage specific failure.
 */
@Repository
public class JpaAuthorizationRepository implements AuthorizationRepository {

  /** The SQLSTATE of a unique violation in PostgreSQL. */
  private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

  private final EntityManager entityManager;

  /**
   * Builds the adapter.
   *
   * @param entityManager the entity manager of the persistence context
   */
  public JpaAuthorizationRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Authorization> findByBeneficiaryAndAgreement(
      BeneficiaryId beneficiaryId, AgreementId agreementId) {
    return entityManager
        .createQuery(
            "select authorization from AuthorizationJpaEntity authorization"
                + " where authorization.beneficiaryId = :beneficiaryId"
                + " and authorization.agreementId = :agreementId",
            AuthorizationJpaEntity.class)
        .setParameter("beneficiaryId", beneficiaryId.value())
        .setParameter("agreementId", agreementId.value())
        .getResultStream()
        .findFirst()
        .map(JpaAuthorizationRepository::toDomain);
  }

  @Override
  @Transactional
  public void save(Authorization authorization) {
    try {
      entityManager.persist(toEntity(authorization));
      // The flush is inside the guard: the unique constraint of the pair is
      // checked by PostgreSQL, and only the flush reaches the database.
      entityManager.flush();
    } catch (PersistenceException exception) {
      if (isUniqueViolation(exception)) {
        throw new AuthorizationAlreadyExistsException(
            authorization.beneficiaryId(), authorization.agreementId());
      }
      throw exception;
    }
  }

  /**
   * Tells whether the database refused the write because of a unique constraint.
   *
   * <p>The SQLSTATE {@code 23505} is the unique violation of PostgreSQL. The check
   * walks the causes, because the driver wraps the failure in the exception of the
   * persistence provider.
   *
   * @param exception the failure of the persistence provider
   * @return {@code true} when the write broke a unique constraint
   */
  private static boolean isUniqueViolation(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof SQLException sqlException
          && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
        return true;
      }
      if (cause.getCause() == cause) {
        return false;
      }
    }
    return false;
  }

  private static Authorization toDomain(AuthorizationJpaEntity entity) {
    return new Authorization(
        new Authorization.AuthorizationId(entity.getId()),
        new BeneficiaryId(entity.getBeneficiaryId()),
        new AgreementId(entity.getAgreementId()),
        entity.getCreatedAt());
  }

  private static AuthorizationJpaEntity toEntity(Authorization authorization) {
    return new AuthorizationJpaEntity(
        authorization.id().value(),
        authorization.beneficiaryId().value(),
        authorization.agreementId().value(),
        authorization.createdAt());
  }
}
