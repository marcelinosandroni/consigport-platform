package br.com.consigport.adapters.out.central.dto;

/**
 * Status of a portability operation, as the Central answers it.
 *
 * <p>Mirrors {@code StatusOperacao} of {@code contracts/central-api.yaml}: the status is
 * {@code PENDENTE}, {@code CONFIRMADA} or {@code REJEITADA}, and the reason of the
 * rejection exists only when the status is {@code REJEITADA}.
 *
 * @param status the situation of the operation in the Central
 * @param motivoRejeicao the reason of the rejection, {@code null} unless it was rejected
 */
public record CentralStatusResponse(String status, String motivoRejeicao) {}
