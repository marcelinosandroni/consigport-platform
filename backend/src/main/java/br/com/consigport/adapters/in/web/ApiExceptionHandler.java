package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.ErrorDetail;
import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.domain.shared.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
 * <p>The messages are the pt-BR defaults; the bundles of the locales arrive with the
 * i18n task and replace them without changing the shape of the answer.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private static final Map<String, HttpStatus> STATUS_BY_CODE =
      Map.ofEntries(
          Map.entry(VALIDATION_ERROR, HttpStatus.BAD_REQUEST),
          Map.entry("IDEMPOTENCY_KEY_REQUIRED", HttpStatus.BAD_REQUEST),
          Map.entry("AUTHORIZATION_REQUIRED", HttpStatus.FORBIDDEN),
          Map.entry("BENEFICIARY_NOT_FOUND", HttpStatus.NOT_FOUND),
          Map.entry("OPERATION_NOT_FOUND", HttpStatus.NOT_FOUND),
          Map.entry("IDEMPOTENCY_CONFLICT", HttpStatus.CONFLICT),
          Map.entry("INSTALLMENT_NOT_ELIGIBLE", HttpStatus.UNPROCESSABLE_ENTITY),
          Map.entry("CENTRAL_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE),
          Map.entry(INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));

  private static final Map<String, String> MESSAGES =
      Map.ofEntries(
          Map.entry(VALIDATION_ERROR, "A requisição é inválida."),
          Map.entry("IDEMPOTENCY_KEY_REQUIRED", "O header Idempotency-Key é obrigatório."),
          Map.entry(
              "AUTHORIZATION_REQUIRED",
              "É necessário registrar a autorização antes de consultar a agenda."),
          Map.entry("BENEFICIARY_NOT_FOUND", "Beneficiário não encontrado."),
          Map.entry("OPERATION_NOT_FOUND", "Operação não encontrada."),
          Map.entry(
              "IDEMPOTENCY_CONFLICT", "A chave de idempotência já foi usada com outro conteúdo."),
          Map.entry("INSTALLMENT_NOT_ELIGIBLE", "Uma ou mais parcelas não são elegíveis."),
          Map.entry("CENTRAL_UNAVAILABLE", "A Central de Convênios está indisponível."),
          Map.entry(INTERNAL_ERROR, "Erro interno."));

  private final Clock clock;

  /**
   * Builds the handler.
   *
   * @param clock the clock that dates the errors
   */
  public ApiExceptionHandler(Clock clock) {
    this.clock = clock;
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
    return new ErrorResponse(
        code,
        MESSAGES.getOrDefault(code, MESSAGES.get(INTERNAL_ERROR)),
        LocaleContextHolder.getLocale().toLanguageTag(),
        Instant.now(clock),
        path,
        details);
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
