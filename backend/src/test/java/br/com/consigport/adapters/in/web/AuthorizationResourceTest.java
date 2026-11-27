package br.com.consigport.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.application.authorization.FindAuthorization;
import br.com.consigport.application.authorization.RegisterAuthorization;
import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.support.FixedClockConfig;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The public edge of the opt-in of the beneficiary.
 *
 * <p>The slice loads the resource and the advice of the errors: the use cases are
 * replaced by doubles, so the test proves the wire — the status, the body and the code
 * of the error — without the storage and without the Central.
 */
@WebMvcTest(AuthorizationResource.class)
@Import(FixedClockConfig.class)
class AuthorizationResourceTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-27T11:00:00Z");
  private static final String BODY =
      """
      {
        "beneficiaryId": "BEN-001",
        "agreementId": "AGR-001"
      }
      """;

  @Autowired private MockMvc mvc;

  @MockitoBean private RegisterAuthorization registerAuthorization;
  @MockitoBean private FindAuthorization findAuthorization;

  @Test
  void answersCreatedWhenTheOptInIsNew() throws Exception {
    Authorization authorization = authorization();
    when(registerAuthorization.execute(any(BeneficiaryId.class), any(AgreementId.class)))
        .thenReturn(new RegisterAuthorization.AuthorizationResult(authorization, true));

    mvc.perform(
            post("/api/v1/authorizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.authorizationId").value(authorization.id().value().toString()))
        .andExpect(jsonPath("$.beneficiaryId").value("BEN-001"))
        .andExpect(jsonPath("$.agreementId").value("AGR-001"))
        .andExpect(jsonPath("$.createdAt").value("2026-11-27T11:00:00Z"));
  }

  @Test
  void answersOkWhenTheOptInAlreadyExists() throws Exception {
    when(registerAuthorization.execute(any(BeneficiaryId.class), any(AgreementId.class)))
        .thenReturn(new RegisterAuthorization.AuthorizationResult(authorization(), false));

    mvc.perform(
            post("/api/v1/authorizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.beneficiaryId").value("BEN-001"));
  }

  @Test
  void refusesTheRequestWithoutTheBeneficiary() throws Exception {
    mvc.perform(
            post("/api/v1/authorizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"agreementId": "AGR-001"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details[0].field").value("beneficiaryId"));
  }

  @Test
  void answersTheOptInOfTheBeneficiary() throws Exception {
    when(findAuthorization.findLatest(any(BeneficiaryId.class)))
        .thenReturn(Optional.of(authorization()));

    mvc.perform(get("/api/v1/authorizations/BEN-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.beneficiaryId").value("BEN-001"))
        .andExpect(jsonPath("$.agreementId").value("AGR-001"));
  }

  @Test
  void answersNotFoundWhenTheBeneficiaryNeverAuthorized() throws Exception {
    when(findAuthorization.findLatest(any(BeneficiaryId.class))).thenReturn(Optional.empty());

    mvc.perform(get("/api/v1/authorizations/BEN-404"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("AUTHORIZATION_NOT_FOUND"))
        .andExpect(jsonPath("$.locale").value("pt-BR"))
        .andExpect(jsonPath("$.path").value("/api/v1/authorizations/BEN-404"));
  }

  private static Authorization authorization() {
    return Authorization.register(
        new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"), CREATED_AT);
  }
}
