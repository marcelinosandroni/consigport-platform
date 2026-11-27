package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.agenda.InstallmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The agenda of the beneficiary, as the public API answers it.
 *
 * @param beneficiaryId the beneficiary of the agenda
 * @param installments the installments of the agenda
 */
@Schema(name = "AgendaResponse", requiredProperties = {"beneficiaryId", "installments"})
public record AgendaResponse(String beneficiaryId, List<InstallmentResponse> installments) {

  /**
   * One installment of the agenda.
   *
   * @param installmentId the identity of the installment
   * @param agreementId the agreement of the installment
   * @param amount the amount of the installment
   * @param discountDate the date of the discount
   * @param status the eligibility of the installment
   */
  @Schema(
      name = "Installment",
      requiredProperties = {"installmentId", "agreementId", "amount", "discountDate", "status"})
  public record InstallmentResponse(
      String installmentId,
      String agreementId,
      BigDecimal amount,
      LocalDate discountDate,
      InstallmentStatus status) {}
}
