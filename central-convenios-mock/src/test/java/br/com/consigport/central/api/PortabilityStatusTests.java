package br.com.consigport.central.api;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.central.domain.Operation;
import br.com.consigport.central.store.InMemoryCentralStore;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Covers the asynchronous status: {@code 204} with {@code Retry-After} before the delay,
 * {@code 200} with the terminal status afterwards, and {@code 404} for an unknown
 * protocol. The delay is shortened to 200 ms to keep the test fast.
 */
@SpringBootTest(properties = "mock.confirmation-delay-ms=200")
@AutoConfigureMockMvc
class PortabilityStatusTests {

  private static final long DELAY_TOLERANCE_MS = 600;

  @Autowired private MockMvc mvc;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void pendingOperationAnswersNoContentWithRetryAfter() throws Exception {
    Operation pendente = store.criarOperacao("BEN-001", List.of("PAR-001"), Instant.now());

    mvc.perform(get("/operacoes-portabilidade/" + pendente.protocolo()))
        .andExpect(status().isNoContent())
        .andExpect(header().string("Retry-After", "2"))
        .andExpect(content().string(""));
  }

  @Test
  void confirmedAfterTheDelay() throws Exception {
    String protocolo = submeter("BEN-001", "PAR-001", "PAR-002");
    Thread.sleep(DELAY_TOLERANCE_MS);

    mvc.perform(get("/operacoes-portabilidade/" + protocolo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMADA"))
        .andExpect(jsonPath("$.motivoRejeicao").value(nullValue()));
  }

  @Test
  void rejectedWhenTheBeneficiaryEndsWithRej() throws Exception {
    String protocolo = submeter("BEN-REJ", "PAR-101");
    Thread.sleep(DELAY_TOLERANCE_MS);

    mvc.perform(get("/operacoes-portabilidade/" + protocolo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJEITADA"))
        .andExpect(jsonPath("$.motivoRejeicao").value("MARGEM_CONSIGNADA_INSUFICIENTE"));
  }

  @Test
  void rejectedWhenTheInstallmentsExceedTheMargin() throws Exception {
    String protocolo = submeter("BEN-MARGIN", "PAR-301");
    Thread.sleep(DELAY_TOLERANCE_MS);

    mvc.perform(get("/operacoes-portabilidade/" + protocolo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJEITADA"))
        .andExpect(jsonPath("$.motivoRejeicao").value("MARGEM_CONSIGNADA_INSUFICIENTE"));
  }

  @Test
  void unknownProtocolReturnsNotFound() throws Exception {
    mvc.perform(get("/operacoes-portabilidade/PRT-20260101-9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.codigo").value("OPERACAO_NAO_ENCONTRADA"))
        .andExpect(jsonPath("$.mensagem").value("Operação não encontrada para o protocolo informado."));
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isAccepted())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.protocolo");
  }
}
