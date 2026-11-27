package br.com.consigport.adapters.in.web.dto;

import java.time.Instant;

/**
 * Answer of the registration and of the consultation of an authorization.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}).
 *
 * @param authorizationId the identity of the authorization
 * @param beneficiaryId the beneficiary that granted the consent
 * @param agreementId the agreement covered by the consent
 * @param createdAt the instant of the opt-in
 */
public record AuthorizationResponse(
    String authorizationId, String beneficiaryId, String agreementId, Instant createdAt) {}
