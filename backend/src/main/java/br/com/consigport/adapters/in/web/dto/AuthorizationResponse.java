package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * The opt-in of the beneficiary, as the public API answers it.
 *
 * @param authorizationId the identity of the authorization
 * @param beneficiaryId the beneficiary that granted the consent
 * @param agreementId the agreement of the consent
 * @param createdAt the instant of the opt-in
 */
@Schema(
    name = "Authorization",
    requiredProperties = {"authorizationId", "beneficiaryId", "agreementId", "createdAt"})
public record AuthorizationResponse(
    String authorizationId, String beneficiaryId, String agreementId, Instant createdAt) {}
