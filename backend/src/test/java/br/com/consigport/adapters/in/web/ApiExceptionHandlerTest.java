package br.com.consigport.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.config.MessageSourceConfig;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The translation of every failure into the error of the public API.
 *
 * <p>The error of the public contract always has the same shape: the stable code, the
 * message, the locale, the instant, the path of the request and the details of the
 * validation. The code is never translated and never changes with the locale.
 */
class ApiExceptionHandlerTest {

  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final String PATH = "/api/v1/portability-operations";

  private ApiExceptionHandler handler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler =
        new ApiExceptionHandler(
            Clock.fixed(NOW, ZoneOffset.UTC), MessageSourceConfig.createMessageSource());
    request = new MockHttpServletRequest("GET", PATH);
    LocaleContextHolder.setLocale(Locale.forLanguageTag("pt-BR"));
  }

  @AfterEach
  void tearDown() {
    LocaleContextHolder.resetLocaleContext();
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("failures")
  void answersTheFailureWithItsStableCode(
      String code, HttpStatus status, DomainError failure) {
    ResponseEntity<ErrorResponse> response = handler.handleDomainError(failure, request);

    assertThat(response.getStatusCode()).isEqualTo(status);
    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo(code);
    assertThat(body.message()).isNotBlank();
    assertThat(body.locale()).isEqualTo("pt-BR");
    assertThat(body.timestamp()).isEqualTo(NOW);
    assertThat(body.path()).isEqualTo(PATH);
    assertThat(body.details()).isEmpty();
  }

  @Test
  void answersTheValidationFailureWithTheCodeOfTheValidation() {
    ResponseEntity<ErrorResponse> response =
        handler.handleInvalidArgument(new IllegalArgumentException("invalid"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
  }

  @Test
  void answersTheUnexpectedFailureWithTheInternalCode() {
    ResponseEntity<ErrorResponse> response =
        handler.handleUnexpected(new IllegalStateException("boom"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    assertThat(response.getBody().message()).isNotBlank();
  }

  @ParameterizedTest(name = "{0} speaks {1}")
  @MethodSource("locales")
  void translatesTheMessageAndKeepsTheCode(Locale locale, String expectedMessage) {
    LocaleContextHolder.setLocale(locale);

    ResponseEntity<ErrorResponse> response =
        handler.handleDomainError(
            new DomainError.OperationNotFound(UUID.randomUUID()), request);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("OPERATION_NOT_FOUND");
    assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    assertThat(response.getBody().locale()).isEqualTo(locale.toLanguageTag());
  }

  @ParameterizedTest(name = "{0} has a message in every locale")
  @MethodSource("codes")
  void everyCodeHasAMessageInEveryLocale(String code) {
    MessageSource messages = MessageSourceConfig.createMessageSource();

    for (Locale locale : MessageSourceConfig.SUPPORTED_LOCALES) {
      assertThat(messages.getMessage(code, null, code, locale))
          .as("the message of %s in %s", code, locale)
          .isNotEqualTo(code);
    }
  }

  private static Stream<Arguments> locales() {
    return Stream.of(
        Arguments.of(Locale.forLanguageTag("pt-BR"), "Operação não encontrada."),
        Arguments.of(Locale.ENGLISH, "Operation not found."),
        Arguments.of(Locale.forLanguageTag("es"), "Operación no encontrada."));
  }

  private static Stream<String> codes() {
    return Stream.of(
        "VALIDATION_ERROR",
        "IDEMPOTENCY_KEY_REQUIRED",
        "AUTHORIZATION_REQUIRED",
        "AUTHORIZATION_NOT_FOUND",
        "BENEFICIARY_NOT_FOUND",
        "OPERATION_NOT_FOUND",
        "IDEMPOTENCY_CONFLICT",
        "INSTALLMENT_NOT_ELIGIBLE",
        "CENTRAL_UNAVAILABLE",
        "INTERNAL_ERROR");
  }

  private static Stream<Arguments> failures() {
    return Stream.of(
        Arguments.of(
            "AUTHORIZATION_REQUIRED",
            HttpStatus.FORBIDDEN,
            new DomainError.AuthorizationRequired()),
        Arguments.of(
            "AUTHORIZATION_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            new DomainError.AuthorizationNotFound(new BeneficiaryId("BEN-001"))),
        Arguments.of(
            "BENEFICIARY_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            new DomainError.BeneficiaryNotFound(new BeneficiaryId("BEN-001"))),
        Arguments.of(
            "OPERATION_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            new DomainError.OperationNotFound(UUID.randomUUID())),
        Arguments.of(
            "IDEMPOTENCY_KEY_REQUIRED",
            HttpStatus.BAD_REQUEST,
            new DomainError.IdempotencyKeyRequired()),
        Arguments.of(
            "IDEMPOTENCY_CONFLICT",
            HttpStatus.CONFLICT,
            new DomainError.IdempotencyConflict(
                new IdempotencyKey(UUID.randomUUID()))),
        Arguments.of(
            "INSTALLMENT_NOT_ELIGIBLE",
            HttpStatus.UNPROCESSABLE_ENTITY,
            new DomainError.InstallmentNotEligible(new InstallmentId("PAR-003"))),
        Arguments.of(
            "CENTRAL_UNAVAILABLE",
            HttpStatus.SERVICE_UNAVAILABLE,
            new DomainError.CentralUnavailable("the Central is unavailable")));
  }
}
