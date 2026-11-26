package br.com.consigport.central.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.central.store.InMemoryCentralStore;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Covers the deterministic chaos of task 1.7: the {@code X-Mock-Delay-Ms} and
 * {@code X-Mock-Force-Error} headers and the two delayed status reads of a
 * {@code -TIMEOUT} beneficiary. The delays are shortened to keep the suite fast.
 */
@SpringBootTest(
    properties = {
      "mock.hmac.enabled=false",
      "mock.confirmation-delay-ms=50",
      "mock.timeout-delay-ms=400"
    })
@AutoConfigureMockMvc
class ChaosTests {

  private static final long DELAY_MS = 400;
  private static final long TOLERANCIA_MS = 100;

  @Autowired private MockMvc mvc;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void forcedErrorHeaderRejectsOnlyThatRequest() throws Exception {
    String protocolo = submeter("BEN-001", "PAR-001");

    mvc.perform(get("/operacoes-portabilidade/" + protocolo).header("X-Mock-Force-Error", "503"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.codigo").value("ERRO_FORCADO"));

    Thread.sleep(120);
    mvc.perform(get("/operacoes-portabilidade/" + protocolo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMADA"));
  }

  @Test
  void forcedErrorAppliesToTheAgenda() throws Exception {
    mvc.perform(get("/beneficiarios/BEN-001/agenda").header("X-Mock-Force-Error", "500"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.codigo").value("ERRO_FORCADO"));
  }

  @Test
  void delayHeaderDelaysOnlyThatResponse() throws Exception {
    String protocolo = submeter("BEN-001", "PAR-001");

    long comAtraso = medir(() -> consultar(protocolo, "X-Mock-Delay-Ms", String.valueOf(DELAY_MS)));
    long semAtraso = medir(() -> consultar(protocolo, null, null));

    assertThat(comAtraso).isGreaterThanOrEqualTo(DELAY_MS - TOLERANCIA_MS);
    assertThat(semAtraso).isLessThan(DELAY_MS - TOLERANCIA_MS);
  }

  @Test
  void timeoutBeneficiaryDelaysTheFirstTwoStatusReads() throws Exception {
    String protocolo = submeter("BEN-TIMEOUT", "PAR-201");
    Thread.sleep(120);

    long primeira = medir(() -> consultar(protocolo, null, null));
    long segunda = medir(() -> consultar(protocolo, null, null));
    long terceira = medir(() -> consultar(protocolo, null, null));

    assertThat(primeira).isGreaterThanOrEqualTo(DELAY_MS - TOLERANCIA_MS);
    assertThat(segunda).isGreaterThanOrEqualTo(DELAY_MS - TOLERANCIA_MS);
    assertThat(terceira).isLessThan(DELAY_MS - TOLERANCIA_MS);
  }

  @Test
  void resetClearsTheChaosCounters() throws Exception {
    String protocolo = submeter("BEN-TIMEOUT", "PAR-201");
    Thread.sleep(120);
    consultar(protocolo, null, null);

    mvc.perform(get("/_admin/state"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contadores.leiturasDeStatus['" + protocolo + "']").value(1));

    mvc.perform(post("/_admin/reset")).andExpect(status().isNoContent());

    mvc.perform(get("/_admin/state"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contadores.leiturasDeStatus.length()").value(0));
  }

  private void consultar(String protocolo, String header, String valor) throws Exception {
    var request = get("/operacoes-portabilidade/" + protocolo);
    if (header != null) {
      request.header(header, valor);
    }
    mvc.perform(request).andReturn();
  }

  private static long medir(ThrowingRunnable acao) throws Exception {
    long inicio = System.nanoTime();
    acao.run();
    return (System.nanoTime() - inicio) / 1_000_000;
  }

  private String submeter(String beneficiarioId, String... parcelas) throws Exception {
    String payload =
        """
        {
          "beneficiarioId": "%s",
          "parcelas": [%s],
          "credorCessionario": "ConsigPort",
          "contaRecebimento": {"banco": "001", "agencia": "1234", "conta": "56789-0"},
          "assinatura": "cfd6a19397032e89d21d7a0321725f71ae78985c6cfdfd71eef2d01d5c49510c"
        }
        """
            .formatted(
                beneficiarioId,
                java.util.Arrays.stream(parcelas)
                    .map(parcela -> "\"" + parcela + "\"")
                    .collect(java.util.stream.Collectors.joining(", ")));

    MvcResult result =
        mvc.perform(
                post("/operacoes-portabilidade")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isAccepted())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.protocolo");
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
