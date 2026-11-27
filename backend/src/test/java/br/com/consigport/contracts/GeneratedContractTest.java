package br.com.consigport.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.support.PostgreSqlTestSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * The contract published by the application against the snapshot of the public API.
 *
 * <p>The snapshot {@code contracts/consigport-api.yaml} is the source of truth of the
 * public API: the front, the tests of contract and the documentation read it. The
 * application publishes the same interface in {@code /v3/api-docs}, and this test is
 * the proof that the two do not drift apart: a route, a status, a field or a parameter
 * that changes in the code and not in the snapshot breaks the build.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GeneratedContractTest extends PostgreSqlTestSupport {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

  @Autowired private TestRestTemplate rest;

  @Test
  void theGeneratedContractMatchesTheSnapshot() throws Exception {
    String published = rest.getForObject("/v3/api-docs", String.class);
    Map<String, Object> generated = JSON.readValue(published, new TypeReference<>() {});
    Map<String, Object> contract = YAML.readValue(snapshot().toFile(), new TypeReference<>() {});

    List<String> differences = ContractComparator.compare(contract, generated);

    assertThat(differences).as("the contract published by the application and the snapshot").isEmpty();
  }

  /** The snapshot of the public API, found from the module of the test. */
  private static Path snapshot() {
    Path current = Path.of("").toAbsolutePath();

    while (current != null) {
      Path candidate = current.resolve("contracts/consigport-api.yaml");
      if (Files.isRegularFile(candidate)) {
        return candidate;
      }
      current = current.getParent();
    }
    throw new IllegalStateException(
        "The snapshot contracts/consigport-api.yaml was not found from " + Path.of("").toAbsolutePath());
  }
}
