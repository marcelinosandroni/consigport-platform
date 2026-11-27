package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The detail of a validation failure, by field.
 *
 * @param field the field that failed
 * @param message the explanation, localized for the locale of the caller
 */
@Schema(name = "ErrorDetail", requiredProperties = {"field", "message"})
public record ErrorDetail(String field, String message) {}
