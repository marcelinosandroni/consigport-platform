package br.com.consigport.adapters.out.central;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Canonical form and signature of the requests sent to the Central.
 *
 * <p>The Central verifies the integrity of every business request with HMAC-SHA256 over
 * the canonical string {@code METHOD\nPATH_WITH_QUERY\nTIMESTAMP\nSHA256_HEX(body)} (ADR
 * {@code 0002}). The canonical body is the JSON with sorted keys, without spaces, in
 * UTF-8, and without the {@code assinatura} field: the signature itself never takes part
 * in what it signs.
 *
 * <p>The implementation reproduces the shared vector in
 * {@code contracts/hmac-vector.json}, which the mock of the Central asserts as well.
 */
public final class HmacSigner {

  private static final String ALGORITHM = "HmacSHA256";
  private static final String SIGNATURE_FIELD = "assinatura";

  private final byte[] secret;
  private final ObjectMapper mapper;

  /**
   * Builds the signer.
   *
   * @param secret the shared secret of the environment
   * @throws IllegalArgumentException when the secret is blank
   */
  public HmacSigner(String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("secret must not be blank");
    }
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }

  /**
   * Serializes a payload in the canonical form: JSON with sorted keys and without
   * spaces.
   *
   * <p>The payload goes through a generic tree before the serialization, so the keys of
   * the nested objects are sorted too, exactly like the Central does.
   *
   * @param payload the payload to serialize
   * @return the canonical JSON
   * @throws IllegalArgumentException when the payload cannot be serialized
   */
  public String canonicalJson(Object payload) {
    Object generic = mapper.convertValue(payload, Object.class);
    return write(generic);
  }

  /**
   * Canonicalizes a raw body: the JSON with sorted keys, without spaces and without the
   * {@code assinatura} field.
   *
   * @param rawBody the raw body, {@code null} or blank for an empty body
   * @return the canonical body
   * @throws IllegalArgumentException when the body is not valid JSON
   */
  public String canonicalBody(String rawBody) {
    if (rawBody == null || rawBody.isBlank()) {
      return "";
    }
    Object generic;
    try {
      generic = mapper.readValue(rawBody, Object.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("body is not valid JSON", exception);
    }
    if (generic instanceof Map<?, ?> fields) {
      @SuppressWarnings("unchecked")
      Map<String, Object> mutable = (Map<String, Object>) fields;
      mutable.remove(SIGNATURE_FIELD);
    }
    return write(generic);
  }

  /**
   * Builds the canonical string that the signature covers.
   *
   * @param method the HTTP method
   * @param pathWithQuery the path with the query string
   * @param timestamp the epoch in seconds
   * @param rawBody the raw body of the request
   * @return the canonical string
   */
  public String canonicalString(String method, String pathWithQuery, long timestamp, String rawBody) {
    return method
        + "\n"
        + pathWithQuery
        + "\n"
        + timestamp
        + "\n"
        + sha256Hex(canonicalBody(rawBody));
  }

  /**
   * Signs a request.
   *
   * @param method the HTTP method
   * @param pathWithQuery the path with the query string
   * @param timestamp the epoch in seconds
   * @param rawBody the raw body of the request
   * @return the signature in lowercase hexadecimal
   */
  public String sign(String method, String pathWithQuery, long timestamp, String rawBody) {
    String canonical = canonicalString(method, pathWithQuery, timestamp, rawBody);
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secret, ALGORITHM));
      return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("HMAC-SHA256 is unavailable", exception);
    }
  }

  /**
   * The lowercase hexadecimal SHA-256 of a value.
   *
   * @param value the value to hash
   * @return the hash in lowercase hexadecimal
   */
  public String sha256Hex(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private String write(Object generic) {
    try {
      return mapper.writeValueAsString(generic);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("payload cannot be serialized as canonical JSON", exception);
    }
  }
}
