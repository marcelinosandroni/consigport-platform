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
 * Covers the three idempotency scenarios of BR-07: same key and payload return the same
 * protocol, the same key with another payload is a conflict and a missing key is invalid.
 */
@SpringBootTest(properties = "mock.hmac.enabled=false")
@AutoConfigureMockMvc
class IdempotencyTests {

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
  void sameKeyAndPayloadReturnTheSameProtocol() throws Exception {
    String key = UUID.randomUUID().toString();

    String primeiro = submeter(key, OPERACAO);
    String segundo = submeter(key, OPERACAO);

    assertThat(segundo).isEqualTo(primeiro);
    mvc.perform(get("/_admin/state"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operacoes.length()").value(1));
  }

  @Test
  void sameKeyWithAnotherPayloadReturnsConflict() throws Exception {
    String key = UUID.randomUUID().toString();
    submeter(key, OPERACAO);

    mvc.perform(
            post("/operacoes-portabilidade")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(OPERACAO.replace("PAR-002", "PAR-003")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.codigo").value("IDEMPOTENCIA_CONFLITO"))
        .andExpect(jsonPath("$.mensagem").value("A Idempotency-Key já foi usada com outro payload."));
  }

  @Test
  void missingKeyReturnsBadRequest() throws Exception {
    mvc.perform(
            post("/operacoes-portabilidade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OPERACAO))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
        .andExpect(jsonPath("$.mensagem").value("O header Idempotency-Key é obrigatório."));
  }

  private String submeter(String key, String payload) throws Exception {
    MvcResult result =
        mvc.perform(
                post("/operacoes-portabilidade")
                    .header("Idempotency-Key", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isAccepted())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.protocolo");
  }
}
