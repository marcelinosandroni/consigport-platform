package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;
import java.util.List;

/**
 * Read model of one operation in the consultation of a single operation.
 *
 * <p>The detail carries the whole snapshot of the request: the installments frozen at
 * the moment of the request, the creditor, the account that receives the portability,
 * the reason of the rejection, the count of the integration attempts and the instant of
 * the update of the account. Like {@link OperationSummary}, it carries the instant of
 * the last write of the row, which belongs to the storage and not to the aggregate.
 *
 * @param id the identity of the operation
 * @param protocol the protocol of the Central, {@code null} while the operation was not
 *     accepted
 * @param beneficiaryId the beneficiary that owns the installments
 * @param agreementId the agreement of the installments
 * @param installments the frozen installments; at least one
 * @param assigneeCreditor the assignee creditor returned by the Central
 * @param receivingAccount the account that receives the portability
 * @param totalAmount the sum of the installments
 * @param status the current status
 * @param rejectionReason the reason of the rejection, only when the operation was
 *     rejected
 * @param retryCount how many integration attempts already failed
 * @param accountUpdatedAt when the receiving account was updated
 * @param createdAt the instant of the creation
 * @param updatedAt the instant of the last write of the operation
 */
public record OperationDetail(
    PortabilityOperation.OperationId id,
    String protocol,
    BeneficiaryId beneficiaryId,
    AgreementId agreementId,
    List<InstallmentSnapshot> installments,
    String assigneeCreditor,
    ReceivingAccount receivingAccount,
    Money totalAmount,
    OperationStatus status,
    RejectionReason rejectionReason,
    int retryCount,
    Instant accountUpdatedAt,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Validates the read model and freezes the snapshot of the installments.
   *
   * @param id the identity of the operation
   * @param beneficiaryId the beneficiary that owns the installments
   * @param agreementId the agreement of the installments
   * @param installments the frozen installments; at least one
   * @param assigneeCreditor the assignee creditor returned by the Central
   * @param receivingAccount the account that receives the portability
   * @param totalAmount the sum of the installments
   * @param status the current status
   * @param createdAt the instant of the creation
   * @param updatedAt the instant of the last write of the operation
   * @throws IllegalArgumentException when any invariant is broken
   */
  public OperationDetail {
    if (id == null) {
      throw new IllegalArgumentException("operationId must not be null");
    }
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    if (agreementId == null) {
      throw new IllegalArgumentException("agreementId must not be null");
    }
    if (installments == null || installments.isEmpty()) {
      throw new IllegalArgumentException("installments must have at least one element");
    }
    installments = List.copyOf(installments);
    if (assigneeCreditor == null || assigneeCreditor.isBlank()) {
      throw new IllegalArgumentException("assigneeCreditor must not be blank");
    }
    if (receivingAccount == null) {
      throw new IllegalArgumentException("receivingAccount must not be null");
    }
    if (totalAmount == null) {
      throw new IllegalArgumentException("totalAmount must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (status == OperationStatus.REJECTED && rejectionReason == null) {
      throw new IllegalArgumentException("rejectionReason must not be null when rejected");
    }
    if (status != OperationStatus.REJECTED && rejectionReason != null) {
      throw new IllegalArgumentException("rejectionReason must be null unless rejected");
    }
    if (status == OperationStatus.COMPLETED && accountUpdatedAt == null) {
      throw new IllegalArgumentException("accountUpdatedAt must not be null when completed");
    }
    if (status != OperationStatus.COMPLETED && accountUpdatedAt != null) {
      throw new IllegalArgumentException("accountUpdatedAt must be null unless completed");
    }
    if (retryCount < 0) {
      throw new IllegalArgumentException("retryCount must not be negative");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
    if (updatedAt == null) {
      throw new IllegalArgumentException("updatedAt must not be null");
    }
  }
}
