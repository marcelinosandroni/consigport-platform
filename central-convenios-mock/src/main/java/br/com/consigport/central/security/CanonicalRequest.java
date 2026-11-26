package br.com.consigport.central.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Canonical form shared by the backend and the mock, as defined in
 * {@code contracts/hmac-vector.json}.
 *
 * <p>The signed body is JSON with sorted keys, no spaces and UTF-8; the {@code assinatura}
 * field does not take part in the signature. The canonical string is:
 * {@code METHOD\nPATH_WITH_QUERY\nTIMESTAMP\nSHA256_HEX(body)}.
 */
public final class CanonicalRequest {

  /** Request attribute that carries the canonical body to the controllers. */
  public static final String CANONICAL_BODY_ATTRIBUTE = "mock.canonicalBody";

  private static final ObjectMapper MAPPER =
      new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

  private CanonicalRequest() {
    // Utility class.
  }

  /** Returns the compact JSON with sorted keys, without the {@code assinatura} field. */
  public static String canonicalJson(String rawBody) {
    if (rawBody == null || rawBody.isBlank()) {
      return "";
    }
    try {
      Object generic = MAPPER.readValue(rawBody, Object.class);
      if (generic instanceof Map<?, ?> mapa) {
        @SuppressWarnings("unchecked")
        Map<String, Object> campos = (Map<String, Object>) mapa;
        campos.remove("assinatura");
      }
      return MAPPER.writeValueAsString(generic);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Corpo JSON inválido.", exception);
    }
  }

  /** Returns the lowercase hexadecimal SHA-256 of the given value. */
  public static String sha256Hex(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponível.", exception);
    }
  }

  /** Builds the canonical string signed by the HMAC. */
  public static String canonicalString(
      String method, String pathWithQuery, long timestamp, String rawBody) {
    return method
        + "\n"
        + pathWithQuery
        + "\n"
        + timestamp
        + "\n"
        + sha256Hex(canonicalJson(rawBody));
  }
}
