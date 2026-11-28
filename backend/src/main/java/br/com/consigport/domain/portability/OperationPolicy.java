package br.com.consigport.domain.portability;

import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.InstallmentId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Policy of the eligibility of the installments (BR-03, BR-04, BR-05).
 *
 * <p>Only the installments that the agenda reports as {@code OPEN} are eligible, and
 * only the ones of the requested agreement. The policy freezes the snapshot of the
 * eligible installments with the amounts and the dates the agenda had at the moment of
 * the request (BR-05).
 */
public final class OperationPolicy {

  /**
   * Freezes the snapshot of the requested installments.
   *
   * @param request the request of the operation
   * @param agenda the agenda of the beneficiary
   * @return the frozen installments, in the order of the request
   * @throws DomainError.InstallmentNotEligible when an installment is unknown, closed or
   *     belongs to another agreement
   */
  public List<InstallmentSnapshot> freeze(OperationRequest request, BeneficiaryAgenda agenda) {
    Map<InstallmentId, Installment> byId =
        agenda.installments().stream()
            .collect(Collectors.toMap(Installment::installmentId, Function.identity()));
    return request.installmentIds().stream()
        .map(installmentId -> freeze(request.agreementId(), installmentId, byId))
        .toList();
  }

  private static InstallmentSnapshot freeze(
      AgreementId agreementId, InstallmentId installmentId, Map<InstallmentId, Installment> byId) {
    Installment installment = byId.get(installmentId);
    if (installment == null
        || installment.status() != InstallmentStatus.OPEN
        || !installment.agreementId().equals(agreementId)) {
      throw new DomainError.InstallmentNotEligible(installmentId);
    }
    return new InstallmentSnapshot(
        installment.installmentId(),
        installment.agreementId(),
        installment.amount(),
        installment.discountDate());
  }
}
