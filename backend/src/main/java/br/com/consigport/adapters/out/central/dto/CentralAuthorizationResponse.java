package br.com.consigport.adapters.out.central.dto;

/**
 * Answer of the Central that registers the opt-in.
 *
 * <p>The Central answers {@code 201} when the consent is new and {@code 200} when the
 * pair was already authorized; the identifier is the same in both cases.
 *
 * @param autorizacaoId the identifier of the authorization in the Central
 */
public record CentralAuthorizationResponse(String autorizacaoId) {}
