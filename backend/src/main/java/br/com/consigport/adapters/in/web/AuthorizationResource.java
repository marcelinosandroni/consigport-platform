package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.AuthorizationRequest;
import br.com.consigport.adapters.in.web.dto.AuthorizationResponse;
import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.application.authorization.FindAuthorization;
import br.com.consigport.application.authorization.RegisterAuthorization;
import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public edge of the opt-in of the beneficiary.
 *
 * <p>The registration answers {@code 201} when the opt-in is new and {@code 200} when
 * the pair was already authorized: the opt-in is idempotent, so repeating it never
 * creates a second record.
 */
@RestController
@RequestMapping("/api/v1/authorizations")
@Tag(name = "Authorizations", description = "Opt-in do beneficiário para consulta da agenda.")
public class AuthorizationResource {

  private final RegisterAuthorization registerAuthorization;
  private final FindAuthorization findAuthorization;

  /**
   * Builds the resource.
   *
   * @param registerAuthorization the use case that registers the opt-in
   * @param findAuthorization the use case that consults the opt-in
   */
  public AuthorizationResource(
      RegisterAuthorization registerAuthorization, FindAuthorization findAuthorization) {
    this.registerAuthorization = registerAuthorization;
    this.findAuthorization = findAuthorization;
  }

  /**
   * Registers the opt-in.
   *
   * @param request the request of the opt-in
   * @return the authorization, created or already existing
   */
  @PostMapping
  @Operation(
      operationId = "registerAuthorization",
      summary = "Registra a autorização (opt-in) do beneficiário",
      description =
          """
          Registra o consentimento do beneficiário para um convênio. O par
          `(beneficiaryId, agreementId)` é único: repetir a chamada devolve `200` com
          a autorização existente em vez de criar outra.
          """)
  @ApiResponse(
      responseCode = "201",
      description = "Autorização criada.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AuthorizationResponse.class)))
  @ApiResponse(
      responseCode = "200",
      description = "Autorização já existente para o par beneficiário/convênio.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AuthorizationResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Requisição inválida.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Erro interno inesperado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<AuthorizationResponse> register(
      @Valid @RequestBody AuthorizationRequest request) {
    RegisterAuthorization.AuthorizationResult result =
        registerAuthorization.execute(
            new BeneficiaryId(request.beneficiaryId()), new AgreementId(request.agreementId()));

    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(toResponse(result.authorization()));
  }

  /**
   * Finds the opt-in of a beneficiary.
   *
   * @param beneficiaryId the beneficiary that granted the consent
   * @return the authorization of the beneficiary
   * @throws DomainError.AuthorizationNotFound when the beneficiary never authorized
   */
  @GetMapping("/{beneficiaryId}")
  @Operation(
      operationId = "findAuthorization",
      summary = "Consulta a autorização de um beneficiário",
      description = "Devolve a autorização registrada para o beneficiário.")
  @ApiResponse(
      responseCode = "200",
      description = "Autorização encontrada.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AuthorizationResponse.class)))
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
  public AuthorizationResponse find(@PathVariable String beneficiaryId) {
    BeneficiaryId id = new BeneficiaryId(beneficiaryId);
    Authorization authorization =
        findAuthorization
            .findLatest(id)
            .orElseThrow(() -> new DomainError.AuthorizationNotFound(id));

    return toResponse(authorization);
  }

  private static AuthorizationResponse toResponse(Authorization authorization) {
    return new AuthorizationResponse(
        authorization.id().value().toString(),
        authorization.beneficiaryId().value(),
        authorization.agreementId().value(),
        authorization.createdAt());
  }
}
