package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.InstallmentId;
import java.time.LocalDate;

/**
 * Frozen picture of one installment at the moment the operation was requested (BR-05).
 *
 * <p>The snapshot never changes: the amounts and dates read from the Central belong to
 * the request, not to the current agenda.
 */
public record InstallmentSnapshot(
    InstallmentId installmentId, AgreementId agreementId, Money amount, LocalDate discountDate) {

  /**
   * Validates the snapshot.
   *
   * @param installmentId the installment identifier
   * @param agreementId the agreement the installment belongs to
   * @param amount the frozen amount
   * @param discountDate the frozen discount date
   * @throws IllegalArgumentException when any field is {@code null}
   */
  public InstallmentSnapshot {
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
  }
}
