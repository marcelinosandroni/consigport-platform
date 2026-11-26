package br.com.consigport.central;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Proves that the contract published inside the mock is identical to the shared contract
 * in {@code contracts/central-api.yaml}, as required by task 1.7.
 *
 * <p>The exported copy is what the mock ships at
 * {@code /openapi/central-api.yaml}; keeping it byte-identical avoids drift between the
 * documentation and the simulated service.
 */
class ContractTest {

  @Test
  void exportedContractMatchesTheSharedContract() throws Exception {
    String shared = Files.readString(sharedContractPath(), StandardCharsets.UTF_8);
    String exported;

    try (InputStream resource = getClass().getResourceAsStream("/openapi/central-api.yaml")) {
      assertThat(resource).as("contrato exportado no classpath").isNotNull();
      exported = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
    }

    assertThat(exported).isEqualTo(shared);
  }

  private static Path sharedContractPath() {
    Path direct = Path.of("contracts", "central-api.yaml");
    return Files.exists(direct) ? direct : Path.of("..", "contracts", "central-api.yaml");
  }
}
