package br.com.consigport.central.security;

import br.com.consigport.central.api.CentralException;
import br.com.consigport.central.api.dto.ErroCentralResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the HMAC of the business endpoints and keeps the body readable downstream.
 *
 * <p>Administrative, actuator and OpenAPI endpoints stay open. When
 * {@code mock.hmac.enabled} is false the filter still caches the canonical body, which
 * the idempotency logic reuses, but skips the signature check (used by focused tests).
 */
@Component
public class HmacFilter extends OncePerRequestFilter {

  private final HmacValidator validator;
  private final boolean enabled;
  private final ObjectMapper mapper;

  public HmacFilter(
      HmacValidator validator,
      @Value("${mock.hmac.enabled:true}") boolean enabled,
      ObjectMapper mapper) {
    this.validator = validator;
    this.enabled = enabled;
    this.mapper = mapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!protegido(request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }

    byte[] body = request.getInputStream().readAllBytes();
    CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, body);
    String rawBody = new String(body, StandardCharsets.UTF_8);

    String canonical;
    try {
      canonical = CanonicalRequest.canonicalJson(rawBody);
    } catch (IllegalArgumentException exception) {
      escreverErro(response, 400, "REQUISICAO_INVALIDA", "Requisição inválida.");
      return;
    }
    wrapped.setAttribute(CanonicalRequest.CANONICAL_BODY_ATTRIBUTE, canonical);

    if (enabled) {
      try {
        validator.validar(
            request.getMethod(),
            pathWithQuery(request),
            request.getHeader("X-Timestamp"),
            request.getHeader("X-Signature"),
            rawBody);
      } catch (CentralException exception) {
        escreverErro(
            response, exception.status().value(), exception.codigo(), exception.getMessage());
        return;
      }
    }

    chain.doFilter(wrapped, response);
  }

  private static boolean protegido(String path) {
    return path.equals("/autorizacoes")
        || path.startsWith("/beneficiarios/")
        || path.startsWith("/operacoes-portabilidade");
  }

  private static String pathWithQuery(HttpServletRequest request) {
    String query = request.getQueryString();
    return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
  }

  private void escreverErro(
      HttpServletResponse response, int status, String codigo, String mensagem)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    mapper.writeValue(
        response.getWriter(), new ErroCentralResponse(codigo, mensagem, Instant.now()));
  }
}
