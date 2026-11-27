package br.com.consigport.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.application.authorization.FindAuthorization;
import br.com.consigport.application.authorization.RegisterAuthorization;
import br.com.consigport.config.MessageSourceConfig;
import br.com.consigport.support.FixedClockConfig;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The negotiation of the locale at the public edge.
 *
 * <p>The caller asks for the language with {@code Accept-Language} and the answer comes
 * in that language: the message travels localized and the code stays the same, because
 * the code is the contract. A language the API does not speak falls back to pt-BR.
 */
@WebMvcTest(AuthorizationResource.class)
@Import({FixedClockConfig.class, MessageSourceConfig.class})
class LocaleNegotiationTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private RegisterAuthorization registerAuthorization;
  @MockitoBean private FindAuthorization findAuthorization;

  @ParameterizedTest(name = "Accept-Language {0} -> {1}")
  @MethodSource("negotiations")
  void answersTheErrorInTheLocaleOfTheCaller(
      String header, String expectedLocale, String expectedMessage) throws Exception {
    when(findAuthorization.findLatest(any())).thenReturn(Optional.empty());

    var request = get("/api/v1/authorizations/BEN-404");
    if (header != null) {
      request = request.header("Accept-Language", header);
    }

    mvc.perform(request)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("AUTHORIZATION_NOT_FOUND"))
        .andExpect(jsonPath("$.locale").value(expectedLocale))
        .andExpect(jsonPath("$.message").value(expectedMessage));
  }

  @Test
  void translatesTheDetailOfTheValidationToo() throws Exception {
    mvc.perform(
            post("/api/v1/authorizations")
                .header("Accept-Language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"agreementId": "AGR-001"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.locale").value("en"))
        .andExpect(jsonPath("$.message").value("The request is invalid."))
        .andExpect(jsonPath("$.details[0].field").value("beneficiaryId"))
        .andExpect(jsonPath("$.details[0].message").value("must not be blank"));
  }

  private static Stream<Arguments> negotiations() {
    return Stream.of(
        Arguments.of("en", "en", "Authorization not found."),
        Arguments.of("es", "es", "Autorización no encontrada."),
        Arguments.of("pt-BR", "pt-BR", "Autorização não encontrada."),
        Arguments.of("pt", "pt-BR", "Autorização não encontrada."),
        Arguments.of("en-US,en;q=0.9", "en", "Authorization not found."),
        Arguments.of("fr", "pt-BR", "Autorização não encontrada."),
        Arguments.of(null, "pt-BR", "Autorização não encontrada."));
  }
}
