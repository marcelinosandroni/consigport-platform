package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.InstallmentId;
import java.util.List;

/**
 * The request of a portability operation, as the domain understands it (BR-04, BR-06).
 *
 * <p>The request carries the choice of the beneficiary: the beneficiary, the agreement,
 * the selected installments, the assignee creditor and the receiving account. The
 * amounts and dates are not part of the request: they come from the agenda and are
 * frozen in the snapshot of the operation (BR-05).
 *
 * <p>The request also knows what makes two requests the same: the same beneficiary, the
 * same agreement, the same set of installments, the same creditor and the same
 * receiving account. That is what the idempotency of the creation compares (BR-07).
 *
 * @param beneficiaryId the beneficiary that owns the installments
 * @param agreementId the agreement the installments belong to
 * @param installmentIds the selected installments, at least one
 * @param assigneeCreditor the creditor that receives the installments
 * @param receivingAccount the account that receives the portability
 */
public record OperationRequest(
    BeneficiaryId beneficiaryId,
    AgreementId agreementId,
    List<InstallmentId> installmentIds,
    String assigneeCreditor,
    ReceivingAccount receivingAccount) {

  /**
   * Validates the request and freezes the selection.
   *
   * @param beneficiaryId the beneficiary that owns the installments
   * @param agreementId the agreement the installments belong to
   * @param installmentIds the selected installments, at least one
   * @param assigneeCreditor the creditor that receives the installments
   * @param receivingAccount the account that receives the portability
   * @throws IllegalArgumentException when any invariant is broken
   */
  public OperationRequest {
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    if (agreementId == null) {
      throw new IllegalArgumentException("agreementId must not be null");
    }
    if (installmentIds == null || installmentIds.isEmpty()) {
      throw new IllegalArgumentException("installmentIds must have at least one element");
    }
    // The check of the null element comes before the copy: `List.copyOf` refuses a null
    // with a NullPointerException, which would hide the invariant of the domain behind a
    // failure of the library.
    if (installmentIds.stream().anyMatch(installmentId -> installmentId == null)) {
      throw new IllegalArgumentException("installmentIds must not contain null");
    }
    installmentIds = List.copyOf(installmentIds);
    if (installmentIds.stream().distinct().count() != installmentIds.size()) {
      throw new IllegalArgumentException("installmentIds must not repeat an installment");
    }
    if (assigneeCreditor == null || assigneeCreditor.isBlank()) {
      throw new IllegalArgumentException("assigneeCreditor must not be blank");
    }
    assigneeCreditor = assigneeCreditor.trim();
    if (receivingAccount == null) {
      throw new IllegalArgumentException("receivingAccount must not be null");
    }
  }

  /**
   * Tells whether the operation answers this very request.
   *
   * <p>The comparison ignores the order of the installments: the same selection in
   * another order is the same request. The amounts and the dates are not compared: they
   * belong to the snapshot, which changes with the agenda, and the request never
   * carries them.
   *
   * @param operation the stored operation
   * @return {@code true} when the operation answers this request
   */
  public boolean matches(PortabilityOperation operation) {
    if (operation == null) {
      return false;
    }
    return beneficiaryId.equals(operation.beneficiaryId())
        && agreementId.equals(operation.agreementId())
        && assigneeCreditor.equals(operation.assigneeCreditor())
        && receivingAccount.equals(operation.receivingAccount())
        && sortedInstallmentIds().equals(sortedInstallmentIdsOf(operation));
  }

  private List<String> sortedInstallmentIds() {
    return installmentIds.stream().map(InstallmentId::value).sorted().toList();
  }

  private static List<String> sortedInstallmentIdsOf(PortabilityOperation operation) {
    return operation.installments().stream()
        .map(snapshot -> snapshot.installmentId().value())
        .sorted()
        .toList();
  }
}
