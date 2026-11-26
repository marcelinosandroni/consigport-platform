package br.com.consigport.domain.authorization;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.util.Optional;

/**
 * Port of the authorization storage.
 *
 * <p>Declared in the domain and implemented by the persistence adapter: the domain never
 * knows the storage technology. The contract is the one of BR-02: one authorization per
 * {@code (beneficiaryId, agreementId)}.
 */
public interface AuthorizationRepository {

  /**
   * Finds the authorization of a beneficiary for one agreement.
   *
   * @param beneficiaryId the beneficiary that gave the opt-in
   * @param agreementId the agreement the opt-in covers
   * @return the authorization, or empty when the pair was never authorized
   */
  Optional<Authorization> findByBeneficiaryAndAgreement(
      BeneficiaryId beneficiaryId, AgreementId agreementId);

  /**
   * Stores a new authorization.
   *
   * @param authorization the authorization to store
   * @throws AuthorizationAlreadyExistsException when the pair
   *     {@code (beneficiaryId, agreementId)} is already authorized
   */
  void save(Authorization authorization);
}
