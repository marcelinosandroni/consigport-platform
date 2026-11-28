package br.com.consigport.domain.authorization;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;

/**
 * Raised when an opt-in is stored for a pair that is already authorized (BR-02).
 *
 * <p>The persistence adapter translates the unique constraint of the database into this
 * exception, so the application layer never sees a storage specific error.
 */
public final class AuthorizationAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient BeneficiaryId beneficiaryId;
  private final transient AgreementId agreementId;

  /**
   * Builds the exception for the offending pair.
   *
   * @param beneficiaryId the beneficiary that is already authorized
   * @param agreementId the agreement that is already authorized
   */
  public AuthorizationAlreadyExistsException(BeneficiaryId beneficiaryId, AgreementId agreementId) {
    super(
        "authorization already exists for beneficiary "
            + beneficiaryId.value()
            + " and agreement "
            + agreementId.value());
    this.beneficiaryId = beneficiaryId;
    this.agreementId = agreementId;
  }

  /**
   * The beneficiary that is already authorized.
   *
   * @return the beneficiary
   */
  public BeneficiaryId beneficiaryId() {
    return beneficiaryId;
  }

  /**
   * The agreement that is already authorized.
   *
   * @return the agreement
   */
  public AgreementId agreementId() {
    return agreementId;
  }
}
