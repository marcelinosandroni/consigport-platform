package br.com.consigport.adapters.in.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Body of an error of the public API.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The code is stable and is never translated;
 * the message follows the negotiated locale.
 *
 * @param code the stable code of the error
 * @param message the localized message
 * @param locale the negotiated locale
 * @param timestamp the instant of the error
 * @param path the path of the request
 * @param details the details of the validation, by field
 */
public record ErrorResponse(
    String code,
    String message,
    String locale,
    Instant timestamp,
    String path,
    List<ErrorDetail> details) {}
