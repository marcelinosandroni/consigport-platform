package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.AgendaResponse;
import br.com.consigport.adapters.in.web.dto.AgendaResponse.InstallmentResponse;
import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.application.agenda.QueryAgenda;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
@Tag(name = "Agenda", description = "Agenda de descontos futuros de um beneficiário autorizado.")
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
  @Operation(
      operationId = "getBeneficiaryAgenda",
      summary = "Consulta a agenda de descontos futuros",
      description =
          """
          Devolve as parcelas futuras do beneficiário na Central. Exige autorização
          registrada; apenas parcelas `OPEN` são elegíveis para portabilidade.
          """)
  @ApiResponse(
      responseCode = "200",
      description = "Agenda do beneficiário.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AgendaResponse.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Beneficiário sem autorização registrada.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Recurso não encontrado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Erro interno inesperado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
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
