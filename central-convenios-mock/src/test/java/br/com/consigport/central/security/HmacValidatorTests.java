package br.com.consigport.central.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.central.api.CentralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Proves the HMAC implementation against the shared vector in
 * {@code contracts/hmac-vector.json}, which the backend must reproduce byte for byte.
 */
class HmacValidatorTests {

  private static final String SECRET = "consigport-hmac-test-secret-2026";

  private final HmacValidator validator = new HmacValidator(SECRET, 300);
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void reproducesEverySharedVector() throws Exception {
    JsonNode root = mapper.readTree(vectorPath().toFile());

    for (JsonNode vector : root.get("vectors")) {
      String name = vector.get("name").asText();
      String method = vector.get("method").asText();
      String path = vector.get("path").asText();
      long timestamp = vector.get("timestamp").asLong();
      String body = vector.get("body").asText();

      assertThat(CanonicalRequest.canonicalString(method, path, timestamp, body))
          .as("canonical of %s", name)
          .isEqualTo(vector.get("canonical").asText());
      assertThat(validator.assinar(method, path, timestamp, body))
          .as("signature of %s", name)
          .isEqualTo(vector.get("signature").asText());
    }
  }

  @Test
  void rejectsDivergentSignature() {
    long timestamp = Instant.now().getEpochSecond();
    String body = "{\"beneficiarioId\":\"BEN-001\"}";

    assertThatThrownBy(
            () ->
                validator.validar(
                    "POST", "/autorizacoes", String.valueOf(timestamp), "0".repeat(64), body))
        .isInstanceOfSatisfying(
            CentralException.class,
            exception -> assertThat(exception.codigo()).isEqualTo("ASSINATURA_INVALIDA"));
  }

  @Test
  void rejectsTimestampOutsideTolerance() {
    long stale = Instant.now().getEpochSecond() - 301;
    String body = "{}";
    String signature = validator.assinar("POST", "/autorizacoes", stale, body);

    assertThatThrownBy(
            () ->
                validator.validar(
                    "POST", "/autorizacoes", String.valueOf(stale), signature, body))
        .isInstanceOfSatisfying(
            CentralException.class,
            exception -> assertThat(exception.codigo()).isEqualTo("ASSINATURA_INVALIDA"));
  }

  private static Path vectorPath() {
    Path direct = Path.of("contracts", "hmac-vector.json");
    return Files.exists(direct) ? direct : Path.of("..", "contracts", "hmac-vector.json");
  }
}
