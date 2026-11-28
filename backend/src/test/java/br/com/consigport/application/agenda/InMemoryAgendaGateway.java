package br.com.consigport.application.agenda;

import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * In-memory implementation of {@link AgendaGateway}.
 *
 * <p>A hand-written fake: it answers the agenda of a beneficiary and refuses the query
 * the way the Central refuses it, so the use case can be tested without the network.
 */
public final class InMemoryAgendaGateway implements AgendaGateway {

  private final BeneficiaryAgenda agenda;
  private final RuntimeException refusal;

  private InMemoryAgendaGateway(BeneficiaryAgenda agenda, RuntimeException refusal) {
    this.agenda = agenda;
    this.refusal = refusal;
  }

  /**
   * A gateway that answers the given agenda.
   *
   * @param agenda the agenda answered to every beneficiary
   * @return the gateway
   */
  public static InMemoryAgendaGateway answering(BeneficiaryAgenda agenda) {
    return new InMemoryAgendaGateway(agenda, null);
  }

  /**
   * A gateway that refuses the query, as the Central refuses without authorization.
   *
   * @param refusal the error thrown by the gateway
   * @return the gateway
   */
  public static InMemoryAgendaGateway refusing(RuntimeException refusal) {
    return new InMemoryAgendaGateway(null, refusal);
  }

  @Override
  public BeneficiaryAgenda findAgenda(BeneficiaryId beneficiaryId) {
    if (refusal != null) {
      throw refusal;
    }
    return agenda;
  }

  /**
   * An agenda with one open and one closed installment.
   *
   * @param beneficiaryId the beneficiary whose agenda is answered
   * @return the agenda
   */
  public static BeneficiaryAgenda sampleAgenda(BeneficiaryId beneficiaryId) {
    return new BeneficiaryAgenda(
        beneficiaryId,
        List.of(
            new Installment(
                new InstallmentId("PAR-001"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05"),
                InstallmentStatus.OPEN),
            new Installment(
                new InstallmentId("PAR-002"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"),
                InstallmentStatus.CLOSED)));
  }
}
