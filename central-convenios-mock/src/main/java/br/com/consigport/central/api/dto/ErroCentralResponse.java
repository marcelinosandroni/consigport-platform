package br.com.consigport.central.api.dto;

import java.time.Instant;

/**
 * Stable error body of the Central ({@code ErroCentral} in the edital contract).
 *
 * @param codigo stable error code in Portuguese, e.g. {@code SEM_AUTORIZACAO}
 * @param mensagem human-readable message in Portuguese
 * @param timestamp instant the error was produced
 */
public record ErroCentralResponse(String codigo, String mensagem, Instant timestamp) {}
