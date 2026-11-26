package br.com.consigport.central.api.dto;

/**
 * Accepted response of {@code POST /operacoes-portabilidade}.
 *
 * @param protocolo protocol used to poll the asynchronous status
 */
public record PortabilityAcceptedResponse(String protocolo) {}
