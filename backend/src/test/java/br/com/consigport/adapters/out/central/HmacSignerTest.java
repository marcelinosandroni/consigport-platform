package br.com.consigport.adapters.out.central;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Proves the HMAC implementation against the shared vector in
 * {@code contracts/hmac-vector.json}, which the mock of the Central asserts as well.
 */
class HmacSignerTest {

  private static final String SECRET = "consigport-hmac-test-secret-2026";

  private final HmacSigner signer = new HmacSigner(SECRET);
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

      assertThat(signer.canonicalString(method, path, timestamp, body))
          .as("canonical of %s", name)
          .isEqualTo(vector.get("canonical").asText());
      assertThat(signer.sign(method, path, timestamp, body))
          .as("signature of %s", name)
          .isEqualTo(vector.get("signature").asText());
    }
  }

  @Test
  void canonicalizesTheBodyWithoutTheSignatureField() {
    String body =
        "{\"beneficiarioId\":\"BEN-001\",\"assinatura\":\"deadbeef\",\"parcelas\":[\"PAR-001\"]}";

    assertThat(signer.canonicalBody(body))
        .isEqualTo("{\"beneficiarioId\":\"BEN-001\",\"parcelas\":[\"PAR-001\"]}");
  }

  @Test
  void sortsTheKeysOfTheNestedObjects() {
    String body =
        "{\"contaRecebimento\":{\"conta\":\"56789-0\",\"banco\":\"001\",\"agencia\":\"1234\"}}";

    assertThat(signer.canonicalBody(body))
        .isEqualTo("{\"contaRecebimento\":{\"agencia\":\"1234\",\"banco\":\"001\",\"conta\":\"56789-0\"}}");
  }

  @Test
  void signsTheSameBodyWhateverTheOrderOfItsKeys() {
    String first = "{\"beneficiarioId\":\"BEN-001\",\"parcelas\":[\"PAR-001\"]}";
    String second = "{\"parcelas\":[\"PAR-001\"],\"beneficiarioId\":\"BEN-001\"}";

    assertThat(signer.sign("POST", "/operacoes-portabilidade", 1795435205, first))
        .isEqualTo(signer.sign("POST", "/operacoes-portabilidade", 1795435205, second));
  }

  @Test
  void signsAnEmptyBodyOnAGet() {
    assertThat(signer.canonicalBody(""))
        .isEqualTo("");
    assertThat(signer.canonicalString("GET", "/operacoes-portabilidade/PRT-20261123-0001", 1795435205, ""))
        .isEqualTo(
            "GET\n/operacoes-portabilidade/PRT-20261123-0001\n1795435205\n"
                + "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
  }

  private static Path vectorPath() {
    Path direct = Path.of("contracts", "hmac-vector.json");
    return Files.exists(direct) ? direct : Path.of("..", "contracts", "hmac-vector.json");
  }
}
