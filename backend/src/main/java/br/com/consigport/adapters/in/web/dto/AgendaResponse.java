package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.agenda.InstallmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Answer of the consultation of the agenda of a beneficiary.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). Only the {@code OPEN} installments are
 * eligible for a portability operation; the closed ones travel in the answer so the
 * caller sees the whole agenda.
 *
 * @param beneficiaryId the beneficiary whose agenda was read
 * @param installments the installments of the agenda
 */
public record AgendaResponse(String beneficiaryId, List<InstallmentResponse> installments) {

  /**
   * Answer of one installment of the agenda.
   *
   * @param installmentId the identity of the installment
   * @param agreementId the agreement the installment belongs to
   * @param amount the amount of the installment
   * @param discountDate the date of the discount
   * @param status the situation of the installment
   */
  public record InstallmentResponse(
      String installmentId,
      String agreementId,
      BigDecimal amount,
      LocalDate discountDate,
      InstallmentStatus status) {}
}
