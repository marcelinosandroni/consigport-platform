package br.com.consigport.domain.agenda;

import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.InstallmentId;
import java.time.LocalDate;

/**
 * One installment of the future discount agenda of a beneficiary.
 *
 * <p>The installment is a read model: it mirrors what the Central knows about the
 * beneficiary, translated by the anti-corruption layer. Only {@code OPEN} installments
 * are eligible for a portability operation (BR-03).
 *
 * @param installmentId the identifier of the installment
 * @param agreementId the agreement the installment belongs to
 * @param amount the amount of the installment
 * @param discountDate the date of the discount
 * @param status the situation of the installment
 */
public record Installment(
    InstallmentId installmentId,
    AgreementId agreementId,
    Money amount,
    LocalDate discountDate,
    InstallmentStatus status) {

  /**
   * Validates the installment.
   *
   * @param installmentId the identifier of the installment
   * @param agreementId the agreement the installment belongs to
   * @param amount the amount of the installment
   * @param discountDate the date of the discount
   * @param status the situation of the installment
   * @throws IllegalArgumentException when any field is {@code null}
   */
  public Installment {
    if (installmentId == null) {
      throw new IllegalArgumentException("installmentId must not be null");
    }
    if (agreementId == null) {
      throw new IllegalArgumentException("agreementId must not be null");
    }
    if (amount == null) {
      throw new IllegalArgumentException("amount must not be null");
    }
    if (discountDate == null) {
      throw new IllegalArgumentException("discountDate must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
  }
}
