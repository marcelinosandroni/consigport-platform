package br.com.consigport.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Row of the {@code authorizations} table.
 *
 * <p>The entity belongs to the persistence adapter: the domain never sees it. The
 * mapping is explicit, field by field, with no reflection in the domain.
 */
@Entity
@Table(name = "authorizations")
class AuthorizationJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "beneficiary_id", nullable = false, length = 64)
  private String beneficiaryId;

  @Column(name = "agreement_id", nullable = false, length = 64)
  private String agreementId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** Required by Hibernate. */
  protected AuthorizationJpaEntity() {}

  AuthorizationJpaEntity(UUID id, String beneficiaryId, String agreementId, Instant createdAt) {
    this.id = id;
    this.beneficiaryId = beneficiaryId;
    this.agreementId = agreementId;
    this.createdAt = createdAt;
  }

  UUID getId() {
    return id;
  }

  String getBeneficiaryId() {
    return beneficiaryId;
  }

  String getAgreementId() {
    return agreementId;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
