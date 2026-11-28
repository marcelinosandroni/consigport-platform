package br.com.consigport.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Row of the {@code portability_operations} table.
 *
 * <p>The entity belongs to the persistence adapter. The snapshot of the installments is
 * stored as JSONB with the shape of {@code ARCHITECTURE.md} §6, and the status is
 * stored with the name of the domain enum, restricted by the check constraint of the
 * table.
 */
@Entity
@Table(name = "portability_operations")
class PortabilityOperationJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "idempotency_key", nullable = false, length = 64)
  private String idempotencyKey;

  @Column(name = "protocol", length = 64)
  private String protocol;

  @Column(name = "beneficiary_id", nullable = false, length = 64)
  private String beneficiaryId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "installments", nullable = false, columnDefinition = "jsonb")
  private List<InstallmentSnapshotRow> installments;

  @Column(name = "assignee_creditor", nullable = false, length = 255)
  private String assigneeCreditor;

  @Column(name = "receiving_bank", nullable = false, length = 64)
  private String receivingBank;

  @Column(name = "receiving_branch", nullable = false, length = 32)
  private String receivingBranch;

  @Column(name = "receiving_account", nullable = false, length = 64)
  private String receivingAccount;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "rejection_reason", length = 255)
  private String rejectionReason;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "next_poll_at")
  private Instant nextPollAt;

  @Column(name = "account_updated_at")
  private Instant accountUpdatedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  /** Required by Hibernate. */
  protected PortabilityOperationJpaEntity() {}

  UUID getId() {
    return id;
  }

  String getIdempotencyKey() {
    return idempotencyKey;
  }

  String getProtocol() {
    return protocol;
  }

  String getBeneficiaryId() {
    return beneficiaryId;
  }

  List<InstallmentSnapshotRow> getInstallments() {
    return installments;
  }

  String getAssigneeCreditor() {
    return assigneeCreditor;
  }

  String getReceivingBank() {
    return receivingBank;
  }

  String getReceivingBranch() {
    return receivingBranch;
  }

  String getReceivingAccount() {
    return receivingAccount;
  }

  BigDecimal getTotalAmount() {
    return totalAmount;
  }

  String getStatus() {
    return status;
  }

  String getRejectionReason() {
    return rejectionReason;
  }

  int getRetryCount() {
    return retryCount;
  }

  Instant getNextPollAt() {
    return nextPollAt;
  }

  Instant getAccountUpdatedAt() {
    return accountUpdatedAt;
  }

  Instant getCreatedAt() {
    return createdAt;
  }

  Instant getUpdatedAt() {
    return updatedAt;
  }

  long getVersion() {
    return version;
  }

  void setId(UUID id) {
    this.id = id;
  }

  void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  void setBeneficiaryId(String beneficiaryId) {
    this.beneficiaryId = beneficiaryId;
  }

  void setInstallments(List<InstallmentSnapshotRow> installments) {
    this.installments = installments;
  }

  void setAssigneeCreditor(String assigneeCreditor) {
    this.assigneeCreditor = assigneeCreditor;
  }

  void setReceivingBank(String receivingBank) {
    this.receivingBank = receivingBank;
  }

  void setReceivingBranch(String receivingBranch) {
    this.receivingBranch = receivingBranch;
  }

  void setReceivingAccount(String receivingAccount) {
    this.receivingAccount = receivingAccount;
  }

  void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  void setStatus(String status) {
    this.status = status;
  }

  void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  void setNextPollAt(Instant nextPollAt) {
    this.nextPollAt = nextPollAt;
  }

  void setAccountUpdatedAt(Instant accountUpdatedAt) {
    this.accountUpdatedAt = accountUpdatedAt;
  }

  void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
