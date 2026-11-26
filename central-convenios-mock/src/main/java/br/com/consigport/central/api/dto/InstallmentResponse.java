package br.com.consigport.central.api.dto;

import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Installment.InstallmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One installment as published by the edital contract ({@code Parcela}).
 *
 * @param parcelaId identifier of the installment
 * @param convenioId agreement the installment belongs to
 * @param valor amount discounted from the beneficiary
 * @param dataDesconto date of the future discount
 * @param status {@code ABERTA} or {@code FECHADA}
 */
public record InstallmentResponse(
    String parcelaId,
    String convenioId,
    BigDecimal valor,
    LocalDate dataDesconto,
    InstallmentStatus status) {

  public static InstallmentResponse from(Installment installment) {
    return new InstallmentResponse(
        installment.parcelaId(),
        installment.convenioId(),
        installment.valor(),
        installment.dataDesconto(),
        installment.status());
  }
}
