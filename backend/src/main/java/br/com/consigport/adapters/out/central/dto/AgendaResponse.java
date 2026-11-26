package br.com.consigport.adapters.out.central.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parcela as the Central answers it, in Portuguese.
 *
 * <p>This DTO belongs to the outbound adapter and never leaves it: the domain speaks
 * English and the anti-corruption layer translates the contract of the Central.
 *
 * @param installmentId the identifier of the installment, {@code parcelaId}
 * @param agreementId the agreement of the installment, {@code convenioId}
 * @param amount the amount of the installment, {@code valor}
 * @param discountDate the date of the discount, {@code dataDesconto}
 * @param status the situation of the installment, {@code ABERTA} or {@code FECHADA}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgendaResponse(
    @JsonProperty("parcelaId") String installmentId,
    @JsonProperty("convenioId") String agreementId,
    @JsonProperty("valor") BigDecimal amount,
    @JsonProperty("dataDesconto") LocalDate discountDate,
    @JsonProperty("status") String status) {}
