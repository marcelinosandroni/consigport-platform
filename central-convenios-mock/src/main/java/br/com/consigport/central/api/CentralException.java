package br.com.consigport.central.api;

import org.springframework.http.HttpStatus;

/**
 * Domain failure that maps to a stable {@code ErroCentral} body.
 *
 * <p>The code is part of the public contract, so it is never derived from the message.
 */
public class CentralException extends RuntimeException {

  private final HttpStatus status;
  private final String codigo;

  public CentralException(HttpStatus status, String codigo, String mensagem) {
    super(mensagem);
    this.status = status;
    this.codigo = codigo;
  }

  public HttpStatus status() {
    return status;
  }

  public String codigo() {
    return codigo;
  }
}
