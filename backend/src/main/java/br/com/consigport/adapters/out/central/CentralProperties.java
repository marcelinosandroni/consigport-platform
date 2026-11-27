package br.com.consigport.adapters.out.central;

import java.time.Duration;

/**
 * Configuration of the integration with the Central.
 *
 * <p>The values come from the environment: the URL of the Central, the shared secret of
 * the HMAC and the resilience of the client (timeouts, attempts and the delay between
 * them), as {@code ARCHITECTURE.md} §11 defines them.
 *
 * @param baseUrl the base URL of the Central
 * @param hmacSecret the shared secret of the HMAC
 * @param maxAttempts how many times a submission is attempted before it is given up
 * @param retryDelay the delay between two attempts; it doubles on every attempt
 * @param connectTimeout the timeout of the connection
 * @param readTimeout the timeout of the reading
 */
public record CentralProperties(
    String baseUrl,
    String hmacSecret,
    int maxAttempts,
    Duration retryDelay,
    Duration connectTimeout,
    Duration readTimeout) {

  /**
   * Validates the configuration.
   *
   * @throws IllegalArgumentException when a value is missing or out of range
   */
  public CentralProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("baseUrl must not be blank");
    }
    if (hmacSecret == null || hmacSecret.isBlank()) {
      throw new IllegalArgumentException("hmacSecret must not be blank");
    }
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }
    if (retryDelay == null || retryDelay.isNegative()) {
      throw new IllegalArgumentException("retryDelay must not be negative");
    }
    if (connectTimeout == null || connectTimeout.isNegative()) {
      throw new IllegalArgumentException("connectTimeout must not be negative");
    }
    if (readTimeout == null || readTimeout.isNegative()) {
      throw new IllegalArgumentException("readTimeout must not be negative");
    }
  }
}
