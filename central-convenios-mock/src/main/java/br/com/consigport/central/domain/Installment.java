package br.com.consigport.central.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A future payroll installment exposed by the Central.
 *
 * @param parcelaId identifier of the installment, e.g. {@code PAR-001}
 * @param convenioId agreement the installment belongs to, e.g. {@code AGR-001}
 * @param valor amount discounted from the beneficiary
 * @param dataDesconto date of the future discount
 * @param status whether the installment can still be ported
 */
public record Installment(
    String parcelaId,
    String convenioId,
    BigDecimal valor,
    LocalDate dataDesconto,
    InstallmentStatus status) {

  /** Situação da parcela, as published in the edital contract. */
  public enum InstallmentStatus {
    ABERTA,
    FECHADA
  }
}
