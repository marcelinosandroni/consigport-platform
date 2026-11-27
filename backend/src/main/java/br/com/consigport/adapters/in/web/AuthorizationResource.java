package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.AuthorizationRequest;
import br.com.consigport.adapters.in.web.dto.AuthorizationResponse;
import br.com.consigport.application.authorization.FindAuthorization;
import br.com.consigport.application.authorization.RegisterAuthorization;
import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
