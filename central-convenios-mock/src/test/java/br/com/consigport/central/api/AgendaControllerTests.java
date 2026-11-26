package br.com.consigport.central.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.central.domain.Authorization;
import br.com.consigport.central.store.InMemoryCentralStore;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Covers the agenda endpoint: the happy path with opt-in, the unknown beneficiary and
 * the beneficiary that has not granted consent yet (BR-01).
 */
@SpringBootTest(properties = "mock.hmac.enabled=false")
@AutoConfigureMockMvc
class AgendaControllerTests {

  @Autowired private MockMvc mvc;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void authorizedBeneficiarySeesTheThreeOpenInstallments() throws Exception {
    store.registrarAutorizacao(
        new Authorization(
            "AUT-TEST-0001", "BEN-001", "AGR-001", Instant.parse("2026-11-26T10:00:00Z")));

    mvc.perform(get("/beneficiarios/BEN-001/agenda"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].parcelaId").value("PAR-001"))
        .andExpect(jsonPath("$[0].convenioId").value("AGR-001"))
        .andExpect(jsonPath("$[0].valor").value(450.00))
        .andExpect(jsonPath("$[0].dataDesconto").value("2026-12-05"))
        .andExpect(jsonPath("$[0].status").value("ABERTA"))
        .andExpect(jsonPath("$[2].parcelaId").value("PAR-003"))
        .andExpect(jsonPath("$[2].valor").value(1200.00));
  }

  @Test
  void unknownBeneficiaryReturnsNotFound() throws Exception {
    mvc.perform(get("/beneficiarios/BEN-UNKNOWN/agenda"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.codigo").value("BENEFICIARIO_NAO_ENCONTRADO"))
        .andExpect(jsonPath("$.mensagem").value("Beneficiário não encontrado."))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void beneficiaryWithoutAuthorizationReturnsForbidden() throws Exception {
    mvc.perform(get("/beneficiarios/BEN-REJ/agenda"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.codigo").value("SEM_AUTORIZACAO"))
        .andExpect(jsonPath("$.mensagem").value("O beneficiário não possui autorização para este convênio."))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
