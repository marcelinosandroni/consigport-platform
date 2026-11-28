package br.com.consigport.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The cross-origin policy of the public API.
 *
 * <p>The interface runs on its own origin ({@code 5173}) and the browser asks the API
 * directly when it follows an operation. The policy is an allowlist: only the origins
 * of the interface are answered, and only the methods and the headers the contract
 * declares. {@code ARCHITECTURE.md} §13 asks for it restricted to the front in
 * development, and the actuator and the OpenAPI document stay outside of it.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  /**
   * The origins allowed to ask the API from a browser.
   *
   * @param allowedOrigins the comma-separated list of origins
   */
  public CorsConfig(
      @Value("${cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
          String allowedOrigins) {
    this.allowedOrigins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toArray(String[]::new);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("Accept-Language", "Content-Type", "Idempotency-Key")
        .maxAge(3600);
  }
}
