package br.com.consigport.domain.agenda;

import br.com.consigport.domain.shared.BeneficiaryId;

/**
 * Port of the agenda of a beneficiary.
 *
 * <p>The agenda lives in the Central; the domain reads it through this port. The
 * adapter behind it answers {@code AUTHORIZATION_REQUIRED} when the beneficiary has no
 * opt-in (BR-01) and {@code BENEFICIARY_NOT_FOUND} when the Central does not know the
 * beneficiary.
 */
public interface AgendaGateway {

  /**
   * Reads the agenda of a beneficiary.
   *
   * @param beneficiaryId the beneficiary whose agenda is read
   * @return the agenda of the beneficiary
   */
  BeneficiaryAgenda findAgenda(BeneficiaryId beneficiaryId);
}
