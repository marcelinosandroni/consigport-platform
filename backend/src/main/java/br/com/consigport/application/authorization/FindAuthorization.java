package br.com.consigport.application.authorization;

import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.AuthorizationRepository;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.util.Optional;

/**
 * Finds the opt-in of a beneficiary for one agreement (BR-01).
 *
 * <p>The use case answers whether the agenda of the beneficiary may be consulted: the
 * caller needs the authorization to exist for the pair.
 */
public final class FindAuthorization {

  private final AuthorizationRepository repository;

  /**
   * Builds the use case.
   *
   * @param repository the storage of the authorizations
   */
  public FindAuthorization(AuthorizationRepository repository) {
    this.repository = repository;
  }

  /**
   * Finds the authorization of the pair.
   *
   * @param beneficiaryId the beneficiary that granted the consent
   * @param agreementId the agreement covered by the consent
   * @return the authorization, or empty when the pair was never authorized
   */
  public Optional<Authorization> execute(BeneficiaryId beneficiaryId, AgreementId agreementId) {
    return repository.findByBeneficiaryAndAgreement(beneficiaryId, agreementId);
  }

  /**
   * Finds the latest authorization of a beneficiary.
   *
   * <p>The beneficiary can authorize more than one agreement; the consultation of the
   * public API answers one record, so it answers the most recent opt-in.
   *
   * @param beneficiaryId the beneficiary that granted the consent
   * @return the most recent authorization of the beneficiary, or empty when the
   *     beneficiary never authorized
   */
  public Optional<Authorization> findLatest(BeneficiaryId beneficiaryId) {
    return repository.findLatestByBeneficiary(beneficiaryId);
  }
}
