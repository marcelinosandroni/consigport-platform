package br.com.consigport.domain.agenda;

import br.com.consigport.domain.shared.BeneficiaryId;
import java.util.List;

/**
 * Read model of the future discount agenda of a beneficiary.
 *
 * <p>The agenda is immutable and not persisted: it lives in the Central and is read
 * through the {@link AgendaGateway}. Only the {@code OPEN} installments are eligible
 * for a portability operation (BR-03).
 *
 * @param beneficiaryId the beneficiary whose agenda is read
 * @param installments the installments of the agenda
 */
public record BeneficiaryAgenda(BeneficiaryId beneficiaryId, List<Installment> installments) {

  /**
   * Validates and freezes the agenda.
   *
   * @param beneficiaryId the beneficiary whose agenda is read
   * @param installments the installments of the agenda
   * @throws IllegalArgumentException when the beneficiary is {@code null}
   */
  public BeneficiaryAgenda {
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    installments = installments == null ? List.of() : List.copyOf(installments);
  }

  /**
   * The installments eligible for a portability operation.
   *
   * @return the open installments
   */
  public List<Installment> openInstallments() {
    return installments.stream()
        .filter(installment -> installment.status() == InstallmentStatus.OPEN)
        .toList();
  }
}
