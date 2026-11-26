package br.com.consigport.central.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.central.store.InMemoryCentralStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Proves that the filter protects the business endpoints: a valid signature passes and a
 * missing, divergent or stale one is rejected with {@code 401 ASSINATURA_INVALIDA}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HmacFilterTests {

  private static final String BODY =
      """
      {
        "beneficiarioId": "BEN-001",
        "parcelas": ["PAR-001"],
        "credorCessionario": "ConsigPort",
        "contaRecebimento": {"banco": "001", "agencia": "1234", "conta": "56789-0"},
        "assinatura": "cfd6a19397032e89d21d7a0321725f71ae78985c6cfdfd71eef2d01d5c49510c"
      }
      """;

  @Autowired private MockMvc mvc;
  @Autowired private HmacValidator validator;
  @Autowired private InMemoryCentralStore store;

  @BeforeEach
  void resetState() {
    store.reset();
  }

  @Test
  void validSignatureIsAccepted() throws Exception {
    long timestamp = Instant.now().getEpochSecond();
    String signature = validator.assinar("POST", "/operacoes-portabilidade", timestamp, BODY);

    mvc.perform(
            post("/operacoes-portabilidade")
                .header("X-Timestamp", timestamp)
                .header("X-Signature", signature)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.protocolo").exists());
  }

  @Test
  void missingSignatureReturnsUnauthorized() throws Exception {
    mvc.perform(
            post("/operacoes-portabilidade")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.codigo").value("ASSINATURA_INVALIDA"));
  }

  @Test
  void divergentSignatureReturnsUnauthorized() throws Exception {
    mvc.perform(
            post("/operacoes-portabilidade")
                .header("X-Timestamp", Instant.now().getEpochSecond())
                .header("X-Signature", "0".repeat(64))
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.codigo").value("ASSINATURA_INVALIDA"));
  }

  @Test
  void staleTimestampReturnsUnauthorized() throws Exception {
    long stale = Instant.now().getEpochSecond() - 301;
    String signature = validator.assinar("POST", "/operacoes-portabilidade", stale, BODY);

    mvc.perform(
            post("/operacoes-portabilidade")
                .header("X-Timestamp", stale)
                .header("X-Signature", signature)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.codigo").value("ASSINATURA_INVALIDA"));
  }

  @Test
  void protectedGetWithoutSignatureReturnsUnauthorized() throws Exception {
    mvc.perform(get("/beneficiarios/BEN-001/agenda"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.codigo").value("ASSINATURA_INVALIDA"));
  }
}
