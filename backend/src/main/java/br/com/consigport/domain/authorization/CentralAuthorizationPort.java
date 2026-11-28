package br.com.consigport.domain.authorization;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;

/**
 * Port of the replication of the opt-in in the Central.
 *
 * <p>The Central keeps its own authorizations; the backend replicates the consent
 * through this port. The replication is idempotent: the Central answers with the
 * existing authorization when the same pair is sent again (BR-02).
 */
public interface CentralAuthorizationPort {

  /**
   * Replicates the opt-in of a beneficiary for one agreement in the Central.
   *
   * @param beneficiaryId the beneficiary that granted the consent
   * @param agreementId the agreement covered by the consent
   */
  void register(BeneficiaryId beneficiaryId, AgreementId agreementId);
}
