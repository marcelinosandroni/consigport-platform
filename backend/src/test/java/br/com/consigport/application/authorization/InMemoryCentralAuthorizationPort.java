package br.com.consigport.application.authorization;

import br.com.consigport.domain.authorization.CentralAuthorizationPort;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of {@link CentralAuthorizationPort}.
 *
 * <p>A hand-written fake: it records the replications, so the test can prove when the
 * use case talks to the Central and when it does not.
 */
public final class InMemoryCentralAuthorizationPort implements CentralAuthorizationPort {

  private final List<String> replications = new ArrayList<>();

  @Override
  public void register(BeneficiaryId beneficiaryId, AgreementId agreementId) {
    replications.add(beneficiaryId.value() + ":" + agreementId.value());
  }

  /**
   * The pairs replicated so far, in order.
   *
   * @return the replicated pairs
   */
  public List<String> replications() {
    return List.copyOf(replications);
  }

  /**
   * How many replications reached the Central.
   *
   * @return the number of replications
   */
  public int replicationCount() {
    return replications.size();
  }
}
