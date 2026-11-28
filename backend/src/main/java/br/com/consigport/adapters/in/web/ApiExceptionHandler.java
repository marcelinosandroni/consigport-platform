package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.ErrorDetail;
import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.domain.shared.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Translation of the failures of the application into the errors of the public API.
 *
 * <p>The error carries the stable code, the localized message, the negotiated locale,
 * the instant, the path of the request and the details of the validation. The code is
 * never translated.
 *
 * <p>The message comes from the bundles of the locales, keyed by the code itself: the
 * caller that asks for {@code en} reads the message in English and the code stays the
 * same.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private static final Map<String, HttpStatus> STATUS_BY_CODE =
      Map.ofEntries(
          Map.entry(VALIDATION_ERROR, HttpStatus.BAD_REQUEST),
          Map.entry("IDEMPOTENCY_KEY_REQUIRED", HttpStatus.BAD_REQUEST),
          Map.entry("AUTHORIZATION_REQUIRED", HttpStatus.FORBIDDEN),
          Map.entry("AUTHORIZATION_NOT_FOUND", HttpStatus.NOT_FOUND),
          Map.entry("BENEFICIARY_NOT_FOUND", HttpStatus.NOT_FOUND),
          Map.entry("OPERATION_NOT_FOUND", HttpStatus.NOT_FOUND),
          Map.entry("IDEMPOTENCY_CONFLICT", HttpStatus.CONFLICT),
          Map.entry("INSTALLMENT_NOT_ELIGIBLE", HttpStatus.UNPROCESSABLE_ENTITY),
          Map.entry("CENTRAL_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE),
          Map.entry(INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));

  private final Clock clock;
  private final MessageSource messageSource;

  /**
   * Builds the handler.
   *
   * @param clock the clock that dates the errors
   * @param messageSource the source of the messages of the locales
   */
  public ApiExceptionHandler(Clock clock, MessageSource messageSource) {
    this.clock = clock;
    this.messageSource = messageSource;
  }

  /**
   * Answers a failure of the domain with the code of the error.
   *
   * @param error the failure of the domain
   * @param request the request that failed
   * @return the error of the public API
   */
  @ExceptionHandler(DomainError.class)
  public ResponseEntity<ErrorResponse> handleDomainError(
      DomainError error, HttpServletRequest request) {
    return answer(error.code(), request, List.of());
  }

  /**
   * Answers a value that the domain refuses with the code of the validation.
   *
   * @param exception the failure of the validation
   * @param request the request that failed
   * @return the error of the public API
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleInvalidArgument(
      IllegalArgumentException exception, HttpServletRequest request) {
    return answer(VALIDATION_ERROR, request, List.of());
  }

  /**
   * Answers a failure that nobody classified.
   *
   * <p>The answer keeps the shape of the error of the public API: the caller always
   * reads the same fields, even when the failure is unexpected. The reason of the
   * failure stays in the log of the server, never in the answer.
   *
   * @param exception the failure that was not classified
   * @param request the request that failed
   * @return the error of the public API
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    LOG.error("unexpected failure on {} {}", request.getMethod(), request.getRequestURI(), exception);
    return answer(INTERNAL_ERROR, request, List.of());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    List<ErrorDetail> details =
        exception.getBindingResult().getFieldErrors().stream()
            .map(ApiExceptionHandler::toDetail)
            .toList();
    return new ResponseEntity<>(
        body(VALIDATION_ERROR, pathOf(request), details), headers, statusCode);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      org.springframework.http.converter.HttpMessageNotReadableException exception,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    return new ResponseEntity<>(
        body(VALIDATION_ERROR, pathOf(request), List.of()), headers, statusCode);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception exception,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    return new ResponseEntity<>(
        body(VALIDATION_ERROR, pathOf(request), List.of()), headers, statusCode);
  }

  private ResponseEntity<ErrorResponse> answer(
      String code, HttpServletRequest request, List<ErrorDetail> details) {
    return ResponseEntity.status(STATUS_BY_CODE.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR))
        .body(body(code, request.getRequestURI(), details));
  }

  private ErrorResponse body(String code, String path, List<ErrorDetail> details) {
    Locale locale = LocaleContextHolder.getLocale();
    return new ErrorResponse(
        code,
        message(code, locale),
        locale.toLanguageTag(),
        Instant.now(clock),
        path,
        details);
  }

  /**
   * The message of the code in the locale of the caller.
   *
   * <p>The key of the bundle is the code itself. A code without a message would answer
   * the code as the message: the test of the bundles proves that every code has one in
   * every locale, so the fallback never happens.
   *
   * @param code the stable code of the error
   * @param locale the negotiated locale
   * @return the message of the error
   */
  private String message(String code, Locale locale) {
    return messageSource.getMessage(code, null, code, locale);
  }

  private static String pathOf(WebRequest request) {
    if (request instanceof ServletWebRequest servletWebRequest) {
      return servletWebRequest.getRequest().getRequestURI();
    }
    return "";
  }

  private static ErrorDetail toDetail(FieldError error) {
    return new ErrorDetail(
        error.getField(),
        error.getDefaultMessage() == null ? "valor inválido" : error.getDefaultMessage());
  }
}
