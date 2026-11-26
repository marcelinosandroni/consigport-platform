package br.com.consigport.central.api.dto;

import br.com.consigport.central.domain.OperationStatus;

/**
 * Status of a portability operation ({@code StatusOperacao} in the contract).
 *
 * @param status {@code PENDENTE}, {@code CONFIRMADA} or {@code REJEITADA}
 * @param motivoRejeicao rejection reason, {@code null} unless the status is {@code REJEITADA}
 */
public record StatusResponse(OperationStatus status, String motivoRejeicao) {}
