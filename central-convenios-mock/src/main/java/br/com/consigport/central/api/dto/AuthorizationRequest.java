package br.com.consigport.central.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Opt-in payload of {@code POST /autorizacoes}.
 *
 * @param beneficiarioId beneficiary granting the consent
 * @param convenioId agreement covered by the consent
 */
public record AuthorizationRequest(
    @NotBlank @Size(max = 64) String beneficiarioId,
    @NotBlank @Size(max = 64) String convenioId) {}
