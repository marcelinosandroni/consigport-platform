package br.com.consigport.central.api;

import br.com.consigport.central.api.dto.ErroCentralResponse;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Renders failures as the {@code ErroCentral} body of the contract.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(CentralException.class)
  public ResponseEntity<ErroCentralResponse> handle(CentralException exception) {
    ErroCentralResponse body =
        new ErroCentralResponse(exception.codigo(), exception.getMessage(), Instant.now());
    return ResponseEntity.status(exception.status()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErroCentralResponse> handleValidation(
      MethodArgumentNotValidException exception) {
    String mensagem =
        exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(ApiExceptionHandler::mensagemDeValidacao)
            .orElse("Requisição inválida.");
    return badRequest(mensagem);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErroCentralResponse> handleUnreadableBody(
      HttpMessageNotReadableException exception) {
    return badRequest("Requisição inválida.");
  }

  private static ResponseEntity<ErroCentralResponse> badRequest(String mensagem) {
    ErroCentralResponse body =
        new ErroCentralResponse("REQUISICAO_INVALIDA", mensagem, Instant.now());
    return ResponseEntity.badRequest().body(body);
  }

  private static String mensagemDeValidacao(FieldError error) {
    if ("Size".equals(error.getCode())) {
      return "O campo %s excede o tamanho máximo.".formatted(error.getField());
    }
    return "O campo %s é obrigatório.".formatted(error.getField());
  }
}
