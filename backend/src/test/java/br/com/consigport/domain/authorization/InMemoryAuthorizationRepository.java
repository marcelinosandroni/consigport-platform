package br.com.consigport.domain.authorization;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link AuthorizationRepository}.
 *
 * <p>A hand-written fake, not a mock: the contract of the port is enforced by the fake
 * itself, so a drift in the interface breaks the compilation instead of hiding behind a
 * runtime stub.
 */
public final class InMemoryAuthorizationRepository implements AuthorizationRepository {

  private final Map<Pair, Authorization> byPair = new HashMap<>();

  @Override
  public Optional<Authorization> findByBeneficiaryAndAgreement(
      BeneficiaryId beneficiaryId, AgreementId agreementId) {
    return Optional.ofNullable(byPair.get(new Pair(beneficiaryId, agreementId)));
  }

  @Override
  public void save(Authorization authorization) {
    Pair key = new Pair(authorization.beneficiaryId(), authorization.agreementId());
    if (byPair.containsKey(key)) {
      throw new AuthorizationAlreadyExistsException(key.beneficiaryId(), key.agreementId());
    }
    byPair.put(key, authorization);
  }

  /**
   * Number of stored authorizations.
   *
   * @return the size of the fake store
   */
  public int size() {
    return byPair.size();
  }

  private record Pair(BeneficiaryId beneficiaryId, AgreementId agreementId) {}
}
