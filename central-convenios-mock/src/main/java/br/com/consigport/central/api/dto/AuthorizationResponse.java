package br.com.consigport.central.api.dto;

/**
 * Opt-in response of {@code POST /autorizacoes}.
 *
 * @param autorizacaoId identifier of the authorization, e.g. {@code AUT-20261126-0001}
 */
public record AuthorizationResponse(String autorizacaoId) {}
