package br.com.consigport.central.api;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.central.store.InMemoryCentralStore;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Covers the opt-in: creation, repetition and validation, plus the agenda unlock that
 * the consent produces (BR-01, BR-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerTests {

  private static final String OPT_IN =
      """
      {"beneficiarioId":"BEN-001","convenioId":"AGR-001"}
      """;

  @Autowired private MockMvc mvc;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void createsTheAuthorizationAndReturnsCreated() throws Exception {
    mvc.perform(post("/autorizacoes").contentType(MediaType.APPLICATION_JSON).content(OPT_IN))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.autorizacaoId").value(matchesPattern("AUT-\\d{8}-0001")));
  }

  @Test
  void repeatingTheOptInReturnsTheSameIdentifier() throws Exception {
    MvcResult first =
        mvc.perform(post("/autorizacoes").contentType(MediaType.APPLICATION_JSON).content(OPT_IN))
            .andExpect(status().isCreated())
            .andReturn();
    String autorizacaoId =
        JsonPath.read(first.getResponse().getContentAsString(), "$.autorizacaoId");

    mvc.perform(post("/autorizacoes").contentType(MediaType.APPLICATION_JSON).content(OPT_IN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autorizacaoId").value(autorizacaoId));
  }

  @Test
  void blankBeneficiaryReturnsBadRequest() throws Exception {
    mvc.perform(
            post("/autorizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"beneficiarioId":"","convenioId":"AGR-001"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
        .andExpect(jsonPath("$.mensagem").value("O campo beneficiarioId é obrigatório."))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void blankAgreementReturnsBadRequest() throws Exception {
    mvc.perform(
            post("/autorizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"beneficiarioId":"BEN-001","convenioId":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
        .andExpect(jsonPath("$.mensagem").value("O campo convenioId é obrigatório."));
  }

  @Test
  void optInUnlocksTheAgenda() throws Exception {
    mvc.perform(post("/autorizacoes").contentType(MediaType.APPLICATION_JSON).content(OPT_IN))
        .andExpect(status().isCreated());

    mvc.perform(get("/beneficiarios/BEN-001/agenda"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }
}
