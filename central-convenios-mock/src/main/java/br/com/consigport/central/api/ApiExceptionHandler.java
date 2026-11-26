package br.com.consigport.central.api;

import br.com.consigport.central.api.dto.ErroCentralResponse;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Renders {@link CentralException} as the {@code ErroCentral} body of the contract.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(CentralException.class)
  public ResponseEntity<ErroCentralResponse> handle(CentralException exception) {
    ErroCentralResponse body =
        new ErroCentralResponse(exception.codigo(), exception.getMessage(), Instant.now());
    return ResponseEntity.status(exception.status()).body(body);
  }
}
