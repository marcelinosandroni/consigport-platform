package br.com.consigport.application.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the agenda query.
 *
 * <p>The use case reads the agenda through the gateway and relays the refusals of the
 * Central as domain errors.
 */
class QueryAgendaTest {

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");

  @Test
  void answersTheAgendaOfTheBeneficiary() {
    BeneficiaryAgenda agenda = InMemoryAgendaGateway.sampleAgenda(BENEFICIARY);
    QueryAgenda useCase =
        new QueryAgenda(InMemoryAgendaGateway.answering(agenda));

    assertThat(useCase.execute(BENEFICIARY)).isEqualTo(agenda);
  }

  @Test
  void relaysTheRefusalWithoutAuthorization() {
    QueryAgenda useCase =
        new QueryAgenda(
            InMemoryAgendaGateway.refusing(new DomainError.AuthorizationRequired()));

    assertThatThrownBy(() -> useCase.execute(BENEFICIARY))
        .isInstanceOf(DomainError.AuthorizationRequired.class);
  }

  @Test
  void relaysTheUnknownBeneficiary() {
    QueryAgenda useCase =
        new QueryAgenda(
            InMemoryAgendaGateway.refusing(new DomainError.BeneficiaryNotFound(BENEFICIARY)));

    assertThatThrownBy(() -> useCase.execute(BENEFICIARY))
        .isInstanceOf(DomainError.BeneficiaryNotFound.class);
  }

  @Test
  void refusesAMissingBeneficiary() {
    QueryAgenda useCase =
        new QueryAgenda(
            InMemoryAgendaGateway.answering(InMemoryAgendaGateway.sampleAgenda(BENEFICIARY)));

    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("beneficiaryId must not be null");
  }
}
