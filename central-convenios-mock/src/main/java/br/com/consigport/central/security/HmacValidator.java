package br.com.consigport.central.security;

import br.com.consigport.central.api.CentralException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Validates the HMAC-SHA256 signature of the Central requests.
 *
 * <p>The secret, the tolerance and the canonical template come from the shared vector
 * ({@code contracts/hmac-vector.json}). A missing, divergent or stale signature is
 * rejected with {@code 401 ASSINATURA_INVALIDA}.
 */
@Component
public class HmacValidator {

  private static final String ALGORITHM = "HmacSHA256";

  private final byte[] secret;
  private final long toleranceSeconds;

  public HmacValidator(
      @Value("${mock.hmac.secret:consigport-hmac-test-secret-2026}") String secret,
      @Value("${mock.hmac.tolerance-seconds:300}") long toleranceSeconds) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.toleranceSeconds = toleranceSeconds;
  }

  /** Rejects the request when the timestamp is stale or the signature does not match. */
  public void validar(
      String method,
      String pathWithQuery,
      String timestampHeader,
      String signature,
      String rawBody) {
    long timestamp = timestamp(timestampHeader);
    if (Math.abs(Instant.now().getEpochSecond() - timestamp) > toleranceSeconds) {
      throw assinaturaInvalida();
    }

    String esperada = assinar(method, pathWithQuery, timestamp, rawBody);
    if (signature == null
        || !MessageDigest.isEqual(
            esperada.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8))) {
      throw assinaturaInvalida();
    }
  }

  /** Signs a request; exposed so tests and the backend can reproduce the vector. */
  public String assinar(String method, String pathWithQuery, long timestamp, String rawBody) {
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secret, ALGORITHM));
      String canonical =
          CanonicalRequest.canonicalString(method, pathWithQuery, timestamp, rawBody);
      return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("HMAC-SHA256 indisponível.", exception);
    }
  }

  private static long timestamp(String timestampHeader) {
    if (timestampHeader == null) {
      throw assinaturaInvalida();
    }
    try {
      return Long.parseLong(timestampHeader);
    } catch (NumberFormatException exception) {
      throw assinaturaInvalida();
    }
  }

  private static CentralException assinaturaInvalida() {
    return new CentralException(
        HttpStatus.UNAUTHORIZED,
        "ASSINATURA_INVALIDA",
        "A assinatura da requisição não confere.");
  }
}
