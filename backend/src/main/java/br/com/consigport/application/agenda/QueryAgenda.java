package br.com.consigport.application.agenda;

import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.shared.BeneficiaryId;

/**
 * Queries the agenda of future discounts of a beneficiary (BR-01, BR-03).
 *
 * <p>The use case reads the agenda through the gateway. The Central refuses the query
 * when the beneficiary has no opt-in and when it does not know the beneficiary; both
 * answers reach the caller as domain errors.
 */
public final class QueryAgenda {

  private final AgendaGateway gateway;

  /**
   * Builds the use case.
   *
   * @param gateway the read of the agenda in the Central
   */
  public QueryAgenda(AgendaGateway gateway) {
    this.gateway = gateway;
  }

  /**
   * Reads the agenda of the beneficiary.
   *
   * @param beneficiaryId the beneficiary whose agenda is read
   * @return the agenda of the beneficiary
   */
  public BeneficiaryAgenda execute(BeneficiaryId beneficiaryId) {
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    return gateway.findAgenda(beneficiaryId);
  }
}
