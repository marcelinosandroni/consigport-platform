package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * The error of the public API.
 *
 * @param code the stable code, never translated
 * @param message the message localized for the locale of the caller
 * @param locale the negotiated locale
 * @param timestamp the instant of the error
 * @param path the path of the request
 * @param details the details of the validation, by field
 */
@Schema(
    name = "ErrorResponse",
    requiredProperties = {"code", "message", "locale", "timestamp", "path", "details"})
public record ErrorResponse(
    String code,
    String message,
    String locale,
    Instant timestamp,
    String path,
    List<ErrorDetail> details) {}
