package br.com.consigport.adapters.out.central.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Answer of the Central when it accepts an operation, in Portuguese.
 *
 * <p>This DTO belongs to the outbound adapter and never leaves it.
 *
 * @param protocolo the protocol of the operation, for the later status query
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralPortabilityResponse(@JsonProperty("protocolo") String protocolo) {}
