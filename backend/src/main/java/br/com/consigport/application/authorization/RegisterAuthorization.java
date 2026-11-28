package br.com.consigport.application.authorization;

import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.AuthorizationAlreadyExistsException;
import br.com.consigport.domain.authorization.AuthorizationRepository;
import br.com.consigport.domain.authorization.CentralAuthorizationPort;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Clock;
import java.util.Optional;

/**
 * Registers the opt-in of a beneficiary for one agreement (BR-02).
 *
 * <p>The use case is idempotent: repeating the opt-in returns the authorization that
 * already exists. The consent is replicated in the Central before the local record is
 * stored, so a failure never leaves a local authorization that the Central does not
 * know about. The Central replication is idempotent on its side, so the retry of a
 * failed request is safe.
 */
public final class RegisterAuthorization {

  private final AuthorizationRepository repository;
  private final CentralAuthorizationPort central;
  private final Clock clock;

  /**
   * Builds the use case.
   *
   * @param repository the storage of the authorizations
   * @param central the replication of the consent in the Central
   * @param clock the clock that dates the opt-in
   */
  public RegisterAuthorization(
      AuthorizationRepository repository, CentralAuthorizationPort central, Clock clock) {
    this.repository = repository;
    this.central = central;
    this.clock = clock;
  }

  /**
   * Registers the opt-in.
   *
   * @param beneficiaryId the beneficiary that granted the consent
   * @param agreementId the agreement covered by the consent
   * @return the authorization, existing or new, and whether it was created now
   */
  public AuthorizationResult execute(BeneficiaryId beneficiaryId, AgreementId agreementId) {
    Optional<Authorization> existing =
        repository.findByBeneficiaryAndAgreement(beneficiaryId, agreementId);
    if (existing.isPresent()) {
      return new AuthorizationResult(existing.get(), false);
    }

    central.register(beneficiaryId, agreementId);
    Authorization authorization =
        Authorization.register(beneficiaryId, agreementId, clock.instant());
    try {
      repository.save(authorization);
    } catch (AuthorizationAlreadyExistsException exception) {
      return new AuthorizationResult(
          repository
              .findByBeneficiaryAndAgreement(beneficiaryId, agreementId)
              .orElseThrow(() -> exception),
          false);
    }
    return new AuthorizationResult(authorization, true);
  }

  /**
   * Answer of the registration of the opt-in.
   *
   * @param authorization the authorization, existing or new
   * @param created whether the opt-in was created by this call
   */
  public record AuthorizationResult(Authorization authorization, boolean created) {}
}
