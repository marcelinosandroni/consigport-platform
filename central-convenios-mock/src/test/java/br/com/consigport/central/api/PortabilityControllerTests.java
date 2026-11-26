package br.com.consigport.central.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
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
 * Covers the acceptance of a portability operation: the {@code 202} with a unique
 * protocol, the initial {@code PENDENTE} state and the {@code 422} for an unknown
 * installment.
 */
@SpringBootTest(properties = "mock.hmac.enabled=false")
@AutoConfigureMockMvc
class PortabilityControllerTests {

  private static final String OPERACAO =
      """
      {
        "beneficiarioId": "BEN-001",
        "parcelas": ["PAR-001", "PAR-002"],
        "credorCessionario": "ConsigPort",
        "contaRecebimento": {"banco": "001", "agencia": "1234", "conta": "56789-0"},
        "assinatura": "cfd6a19397032e89d21d7a0321725f71ae78985c6cfdfd71eef2d01d5c49510c"
      }
      """;

  @Autowired private MockMvc mvc;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void acceptsTheOperationAndStartsPending() throws Exception {
    MvcResult result =
        mvc.perform(
                post("/operacoes-portabilidade")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OPERACAO))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.protocolo").value(matchesPattern("PRT-\\d{8}-0001")))
            .andReturn();
    String protocolo =
        JsonPath.read(result.getResponse().getContentAsString(), "$.protocolo");

    mvc.perform(get("/_admin/state"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operacoes['" + protocolo + "'].status").value("PENDENTE"))
        .andExpect(jsonPath("$.operacoes['" + protocolo + "'].beneficiarioId").value("BEN-001"));
  }

  @Test
  void eachOperationGetsAUniqueProtocol() throws Exception {
    String primeiro = protocoloDa(OPERACAO);
    String segundo = protocoloDa(OPERACAO);

    assertThat(segundo).isNotEqualTo(primeiro);
    assertThat(primeiro).endsWith("-0001");
    assertThat(segundo).endsWith("-0002");
  }

  @Test
  void unknownInstallmentReturnsUnprocessableEntity() throws Exception {
    mvc.perform(
            post("/operacoes-portabilidade")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(OPERACAO.replace("PAR-002", "PAR-999")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.codigo").value("PARCELA_INVALIDA"))
        .andExpect(jsonPath("$.mensagem").value("A parcela PAR-999 não existe."));
  }

  @Test
  void blankBeneficiaryReturnsBadRequest() throws Exception {
    mvc.perform(
            post("/operacoes-portabilidade")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(OPERACAO.replace("\"BEN-001\"", "\"\"")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
  }

  private String protocoloDa(String payload) throws Exception {
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
}
