package br.com.consigport.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Row of the {@code operation_events} table.
 *
 * <p>The entity belongs to the persistence adapter. The trail only grows: the table has
 * a trigger that rejects any update and any delete, and the entity exposes no mutator
 * but the ones the append needs.
 */
@Entity
@Table(name = "operation_events")
class OperationEventJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "operation_id", nullable = false)
  private UUID operationId;

  @Column(name = "previous_status", length = 32)
  private String previousStatus;

  @Column(name = "new_status", nullable = false, length = 32)
  private String newStatus;

  @Column(name = "event_type", nullable = false, length = 48)
  private String eventType;

  @Column(name = "details", nullable = false, columnDefinition = "text")
  private String details;

  /**
   * The raw payload of the Central, as the JSON text of the column.
   *
   * <p>The JSONB type stores the text as it is, so the payload is encoded as a JSON
   * string by {@link RawPayloadCodec} and comes back byte for byte.
   */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload", columnDefinition = "jsonb")
  private String rawPayload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** Required by Hibernate. */
  protected OperationEventJpaEntity() {}

  OperationEventJpaEntity(
      UUID operationId,
      String previousStatus,
      String newStatus,
      String eventType,
      String details,
      String rawPayload,
      Instant createdAt) {
    this.operationId = operationId;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
    this.eventType = eventType;
    this.details = details;
    this.rawPayload = rawPayload;
    this.createdAt = createdAt;
  }

  Long getId() {
    return id;
  }

  UUID getOperationId() {
    return operationId;
  }

  String getPreviousStatus() {
    return previousStatus;
  }

  String getNewStatus() {
    return newStatus;
  }

  String getEventType() {
    return eventType;
  }

  String getDetails() {
    return details;
  }

  String getRawPayload() {
    return rawPayload;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
