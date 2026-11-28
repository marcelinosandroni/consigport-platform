package br.com.consigport.adapters.out.central.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Error answered by the Central, in Portuguese.
 *
 * <p>This DTO belongs to the outbound adapter and never leaves it. The code of the
 * Central is kept as it is: it explains what the Central answered in the trail of the
 * audit, without being translated.
 *
 * @param codigo the stable code of the Central
 * @param mensagem the readable message of the Central
 * @param timestamp the instant of the error
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralErrorResponse(
    @JsonProperty("codigo") String codigo,
    @JsonProperty("mensagem") String mensagem,
    @JsonProperty("timestamp") Instant timestamp) {}
