package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.AgendaResponse;
import br.com.consigport.adapters.in.web.dto.AgendaResponse.InstallmentResponse;
import br.com.consigport.application.agenda.QueryAgenda;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public edge of the agenda of discounts of a beneficiary.
 *
 * <p>The agenda lives in the Central: the beneficiary without opt-in is refused with
 * {@code 403 AUTHORIZATION_REQUIRED} and the beneficiary the Central does not know is
 * refused with {@code 404 BENEFICIARY_NOT_FOUND}.
 */
@RestController
@RequestMapping("/api/v1/beneficiaries")
public class AgendaResource {

  private final QueryAgenda queryAgenda;

  /**
   * Builds the resource.
   *
   * @param queryAgenda the use case that reads the agenda
   */
  public AgendaResource(QueryAgenda queryAgenda) {
    this.queryAgenda = queryAgenda;
  }

  /**
   * Reads the agenda of the beneficiary.
   *
   * @param beneficiaryId the beneficiary whose agenda is read
   * @return the agenda of the beneficiary
   * @throws DomainError.AuthorizationRequired when the beneficiary has no opt-in
   * @throws DomainError.BeneficiaryNotFound when the Central does not know the
   *     beneficiary
   */
  @GetMapping("/{beneficiaryId}/agenda")
  public AgendaResponse agenda(@PathVariable String beneficiaryId) {
    BeneficiaryAgenda agenda = queryAgenda.execute(new BeneficiaryId(beneficiaryId));

    return new AgendaResponse(
        agenda.beneficiaryId().value(),
        agenda.installments().stream().map(AgendaResource::toResponse).toList());
  }

  private static InstallmentResponse toResponse(Installment installment) {
    return new InstallmentResponse(
        installment.installmentId().value(),
        installment.agreementId().value(),
        installment.amount().amount(),
        installment.discountDate(),
        installment.status());
  }
}
