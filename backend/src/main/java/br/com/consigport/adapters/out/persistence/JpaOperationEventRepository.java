package br.com.consigport.adapters.out.persistence;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter of the {@link OperationEventRepository} port over PostgreSQL.
 *
 * <p>The trail only grows: the adapter appends and reads, never updates and never
 * deletes. The database is the last line of defense, with the trigger that rejects any
 * change to a stored event.
 */
@Repository
public class JpaOperationEventRepository implements OperationEventRepository {

  private final EntityManager entityManager;

  /**
   * Builds the adapter.
   *
   * @param entityManager the entity manager of the persistence context
   */
  public JpaOperationEventRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public OperationEvent append(OperationEvent event) {
    OperationEventJpaEntity entity =
        new OperationEventJpaEntity(
            event.operationId().value(),
            event.previousStatus() == null ? null : event.previousStatus().name(),
            event.newStatus().name(),
            event.eventType().name(),
            event.details(),
            RawPayloadCodec.toJsonText(event.rawPayload()),
            event.createdAt());
    entityManager.persist(entity);
    entityManager.flush();
    return toDomain(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OperationEvent> findByOperationId(PortabilityOperation.OperationId operationId) {
    return entityManager
        .createQuery(
            "select event from OperationEventJpaEntity event"
                + " where event.operationId = :operationId"
                + " order by event.id",
            OperationEventJpaEntity.class)
        .setParameter("operationId", operationId.value())
        .getResultStream()
        .map(JpaOperationEventRepository::toDomain)
        .toList();
  }

  private static OperationEvent toDomain(OperationEventJpaEntity entity) {
    return new OperationEvent(
        entity.getId(),
        new PortabilityOperation.OperationId(entity.getOperationId()),
        entity.getPreviousStatus() == null
            ? null
            : OperationStatus.valueOf(entity.getPreviousStatus()),
        OperationStatus.valueOf(entity.getNewStatus()),
        EventType.valueOf(entity.getEventType()),
        entity.getDetails(),
        RawPayloadCodec.toRawText(entity.getRawPayload()),
        entity.getCreatedAt());
  }
}
