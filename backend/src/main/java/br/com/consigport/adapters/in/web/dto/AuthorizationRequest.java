package br.com.consigport.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of the request that registers the opt-in of a beneficiary.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The pair {@code (beneficiaryId,
 * agreementId)} is unique: repeating the request answers the record that already exists.
 *
 * @param beneficiaryId the beneficiary that granted the consent
 * @param agreementId the agreement covered by the consent
 */
public record AuthorizationRequest(
    @NotBlank @Size(max = 64) String beneficiaryId,
    @NotBlank @Size(max = 64) String agreementId) {}
