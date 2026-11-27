package br.com.consigport.adapters.out.central.dto;

/**
 * Body of the request that registers the opt-in in the Central.
 *
 * <p>The Central speaks Portuguese: the beneficiary is {@code beneficiarioId} and the
 * agreement is {@code convenioId}. The DTO never leaves the outbound adapter.
 *
 * @param beneficiarioId the beneficiary that granted the consent
 * @param convenioId the agreement covered by the consent
 */
public record CentralAuthorizationRequest(String beneficiarioId, String convenioId) {}
