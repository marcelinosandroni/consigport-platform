package br.com.consigport.adapters.out.central;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import br.com.consigport.adapters.out.central.acl.CentralAcl;
import br.com.consigport.adapters.out.central.acl.PortabilityAcl;
import br.com.consigport.adapters.out.resilience.CircuitBreaker;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Behaviour of the client of the Central.
 *
 * <p>The signatures are checked against the shared vector: the operation sent below has
 * the exact body of the vector {@code post-portability}, so the signature the client
 * produces is the one the mock of the Central expects ({@code contracts/hmac-vector.json}).
 */
class CentralHttpClientTest {

  private static final String SECRET = "consigport-hmac-test-secret-2026";
  private static final String BASE_URL = "http://central.test";
  private static final long TIMESTAMP = 1795435205L;
  private static final String PROTOCOL = "PRT-20261123-0001";

  /** The signature of the vector {@code post-portability}, for the same body and instant. */
  private static final String VECTOR_SIGNATURE =
      "cfd6a19397032e89d21d7a0321725f71ae78985c6cfdfd71eef2d01d5c49510c";

  /** The body the client sends: the canonical body of the vector with the signature. */
  private static final String EXPECTED_BODY =
      "{\"assinatura\":\""
          + VECTOR_SIGNATURE
          + "\",\"beneficiarioId\":\"BEN-001\",\"contaRecebimento\":{\"agencia\":\"1234\","
          + "\"banco\":\"001\",\"conta\":\"56789-0\"},\"credorCessionario\":\"ConsigPort\","
          + "\"parcelas\":[\"PAR-001\",\"PAR-002\"]}";

  /** The canonical body of the registration of the opt-in. */
  private static final String AUTHORIZATION_BODY =
      "{\"beneficiarioId\":\"BEN-001\",\"convenioId\":\"AGR-001\"}";

  private MockRestServiceServer server;
  private CentralHttpClient client;
  private CircuitBreaker circuit;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    circuit =
        new CircuitBreaker(
            10, 0.5, Duration.ofSeconds(10), 3, Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC));
    client =
        new CentralHttpClient(
            builder.build(),
            new HmacSigner(SECRET),
            new PortabilityAcl(),
            new CentralAcl(),
            Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC),
            new CentralProperties(
                BASE_URL,
                SECRET,
                3,
                Duration.ZERO,
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                Duration.ofSeconds(2)),
            circuit);
  }

  @Test
  void sendsTheSignedOperationAndReturnsTheProtocol() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("X-Signature", VECTOR_SIGNATURE))
        .andExpect(header("X-Timestamp", Long.toString(TIMESTAMP)))
        .andExpect(header("Idempotency-Key", operation().idempotencyKey().value().toString()))
        .andExpect(content().string(EXPECTED_BODY))
        .andRespond(
            withStatus(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"protocolo\":\"" + PROTOCOL + "\"}"));

    String protocol = client.submit(operation());

    assertThat(protocol).isEqualTo(PROTOCOL);
    server.verify();
  }

  @Test
  void retriesTheSubmissionUntilTheCentralAnswers() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andRespond(
            withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"codigo\":\"ERRO_CENTRAL\",\"mensagem\":\"Indisponível.\"}"));
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andRespond(
            withStatus(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"protocolo\":\"" + PROTOCOL + "\"}"));

    String protocol = client.submit(operation());

    assertThat(protocol).isEqualTo(PROTOCOL);
    server.verify();
  }

  @Test
  void givesUpWhenTheCentralKeepsFailing() {
    server
        .expect(ExpectedCount.times(3), requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andRespond(
            withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    "{\"codigo\":\"CENTRAL_INDISPONIVEL\",\"mensagem\":\"Indisponível.\","
                        + "\"timestamp\":\"2026-11-27T10:15:30Z\"}"));

    assertThatThrownBy(() -> client.submit(operation()))
        .isInstanceOfSatisfying(
            CentralSubmissionException.class,
            failure -> {
              assertThat(failure.status()).isEqualTo(503);
              assertThat(failure.code()).isEqualTo("CENTRAL_INDISPONIVEL");
              assertThat(failure.getMessage()).isEqualTo("Indisponível.");
            });
    server.verify();
  }

  @Test
  void doesNotRetryARejectionOfTheCentral() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andRespond(
            withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    "{\"codigo\":\"PARCELA_INVALIDA\",\"mensagem\":\"A parcela não está aberta.\","
                        + "\"timestamp\":\"2026-11-27T10:15:30Z\"}"));

    assertThatThrownBy(() -> client.submit(operation()))
        .isInstanceOfSatisfying(
            CentralSubmissionException.class,
            failure -> {
              assertThat(failure.status()).isEqualTo(422);
              assertThat(failure.code()).isEqualTo("PARCELA_INVALIDA");
            });
    server.verify();
  }

  @Test
  void keepsTheOperationResumableWhenTheAnswerCannotBeRead() {
    server
        .expect(ExpectedCount.times(3), requestTo(BASE_URL + "/operacoes-portabilidade"))
        .andRespond(
            withStatus(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("[\"not-the-contract\"]"));

    assertThatThrownBy(() -> client.submit(operation()))
        .isInstanceOfSatisfying(
            CentralSubmissionException.class,
            failure -> {
              assertThat(failure.status()).isZero();
              assertThat(failure.code()).isEqualTo("CENTRAL_INDISPONIVEL");
            });
    server.verify();
  }

  @Test
  void readsTheAgendaOfTheBeneficiary() {
    server
        .expect(requestTo(BASE_URL + "/beneficiarios/BEN-001/agenda"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("X-Signature", signatureOfTheAgenda()))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    "[{\"parcelaId\":\"PAR-001\",\"convenioId\":\"AGR-001\",\"valor\":450.00,"
                        + "\"dataDesconto\":\"2026-12-05\",\"status\":\"ABERTA\"}]"));

    var agenda = client.findAgenda(new BeneficiaryId("BEN-001"));

    assertThat(agenda.installments()).hasSize(1);
    assertThat(agenda.installments().get(0).installmentId().value()).isEqualTo("PAR-001");
    assertThat(agenda.installments().get(0).amount().amount())
        .isEqualByComparingTo(new BigDecimal("450.00"));
    assertThat(agenda.installments().get(0).status()).isEqualTo(InstallmentStatus.OPEN);
    server.verify();
  }

  @Test
  void translatesTheRefusalOfTheAgendaWithoutAuthorization() {
    server
        .expect(requestTo(BASE_URL + "/beneficiarios/BEN-001/agenda"))
        .andRespond(
            withStatus(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"codigo\":\"SEM_AUTORIZACAO\",\"mensagem\":\"Sem autorização.\"}"));

    assertThatThrownBy(() -> client.findAgenda(new BeneficiaryId("BEN-001")))
        .isInstanceOf(DomainError.AuthorizationRequired.class);
    server.verify();
  }

  @Test
  void translatesTheUnknownBeneficiaryOfTheAgenda() {
    server
        .expect(requestTo(BASE_URL + "/beneficiarios/BEN-404/agenda"))
        .andRespond(
            withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"codigo\":\"BENEFICIARIO_NAO_ENCONTRADO\",\"mensagem\":\"Não encontrado.\"}"));

    assertThatThrownBy(() -> client.findAgenda(new BeneficiaryId("BEN-404")))
        .isInstanceOf(DomainError.BeneficiaryNotFound.class);
    server.verify();
  }

  @Test
  void answersCentralUnavailableWhenTheAgendaCannotBeRead() {
    server
        .expect(requestTo(BASE_URL + "/beneficiarios/BEN-001/agenda"))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"parcelaId\":\"PAR-001\"}"));

    assertThatThrownBy(() -> client.findAgenda(new BeneficiaryId("BEN-001")))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    server.verify();
  }

  @Test
  void answersCentralUnavailableWhenTheCentralDoesNotAnswer() {
    server
        .expect(requestTo(BASE_URL + "/beneficiarios/BEN-001/agenda"))
        .andRespond(
            request -> {
              throw new IOException("connection refused");
            });

    assertThatThrownBy(() -> client.findAgenda(new BeneficiaryId("BEN-001")))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    server.verify();
  }

  @Test
  void readsTheStatusThatIsStillPendingAndHonoursTheRetryAfter() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("X-Signature", signatureOfTheStatus()))
        .andRespond(
            withStatus(HttpStatus.NO_CONTENT).header("Retry-After", "5"));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Pending.class,
        pending -> assertThat(pending.retryAfter()).isEqualTo(Duration.ofSeconds(5)));
    server.verify();
  }

  @Test
  void fallsBackToTheConfiguredIntervalWhenTheCentralSendsNoRetryAfter() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Pending.class,
        pending -> assertThat(pending.retryAfter()).isEqualTo(Duration.ofSeconds(2)));
    server.verify();
  }

  @Test
  void readsTheConfirmationOfTheCentral() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\":\"CONFIRMADA\",\"motivoRejeicao\":null}"));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Confirmed.class,
        confirmed -> assertThat(confirmed.rawPayload()).contains("CONFIRMADA"));
    server.verify();
  }

  @Test
  void readsTheRejectionOfTheCentral() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    "{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"MARGEM_CONSIGNADA_INSUFICIENTE\"}"));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Rejected.class,
        rejected ->
            assertThat(rejected.reason())
                .isEqualTo(new br.com.consigport.domain.portability.RejectionReason.MarginInsufficient()));
    server.verify();
  }

  @Test
  void refusesTheCallsWhileTheCircuitIsOpen() {
    CircuitBreaker shortCircuit =
        new CircuitBreaker(
            2,
            0.5,
            Duration.ofSeconds(10),
            3,
            Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC));
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    CentralHttpClient protectedClient =
        new CentralHttpClient(
            builder.build(),
            new HmacSigner(SECRET),
            new PortabilityAcl(),
            new CentralAcl(),
            Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC),
            new CentralProperties(
                BASE_URL,
                SECRET,
                3,
                Duration.ZERO,
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                Duration.ofSeconds(2)),
            shortCircuit);
    for (int attempt = 0; attempt < 6; attempt++) {
      server
          .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
          .andRespond(
              withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body("{\"codigo\":\"ERRO_CENTRAL\",\"mensagem\":\"Indisponível.\"}"));
    }

    // The first call fails after its three attempts and the second one opens the circuit.
    assertThatThrownBy(() -> protectedClient.statusOf(PROTOCOL))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    assertThat(shortCircuit.isOpen()).isFalse();
    assertThatThrownBy(() -> protectedClient.statusOf(PROTOCOL))
        .isInstanceOf(DomainError.CentralUnavailable.class);

    assertThat(shortCircuit.isOpen()).isTrue();
    assertThatThrownBy(() -> protectedClient.statusOf(PROTOCOL))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    server.verify();
  }

  @Test
  void answersNotFoundWhenTheCentralDoesNotKnowTheProtocol() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(
            withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    "{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\",\"mensagem\":\"Operação não encontrada.\"}"));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.NotFound.class,
        notFound -> {
          assertThat(notFound.protocol()).isEqualTo(PROTOCOL);
          assertThat(notFound.rawPayload()).contains("OPERACAO_NAO_ENCONTRADA");
        });
    server.verify();
  }

  @Test
  void doesNotOpenTheCircuitWhenTheCentralDoesNotKnowTheProtocol() {
    CircuitBreaker shortCircuit =
        new CircuitBreaker(
            2,
            0.5,
            Duration.ofSeconds(10),
            3,
            Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC));
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    CentralHttpClient protectedClient =
        new CentralHttpClient(
            builder.build(),
            new HmacSigner(SECRET),
            new PortabilityAcl(),
            new CentralAcl(),
            Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneOffset.UTC),
            new CentralProperties(
                BASE_URL,
                SECRET,
                3,
                Duration.ZERO,
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                Duration.ofSeconds(2)),
            shortCircuit);
    for (int attempt = 0; attempt < 6; attempt++) {
      server
          .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
          .andRespond(
              withStatus(HttpStatus.NOT_FOUND)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body("{\"codigo\":\"OPERACAO_NAO_ENCONTRADA\"}"));
    }

    // The Central answered every time: it is available, so the circuit never opens.
    for (int attempt = 0; attempt < 6; attempt++) {
      assertThat(protectedClient.statusOf(PROTOCOL)).isInstanceOf(CentralStatus.NotFound.class);
    }

    assertThat(shortCircuit.isOpen()).isFalse();
    server.verify();
  }

  @Test
  void readsTheRejectionWithAReasonTheDomainDoesNotMap() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\":\"REJEITADA\",\"motivoRejeicao\":\"LIMITE_DE_PARCELAS\"}"));

    CentralStatus answer = client.statusOf(PROTOCOL);

    assertThat(answer).isInstanceOfSatisfying(
        CentralStatus.Rejected.class,
        rejected -> {
          assertThat(rejected.reason())
              .isEqualTo(new br.com.consigport.domain.portability.RejectionReason.Unmapped());
          assertThat(rejected.rawPayload()).contains("LIMITE_DE_PARCELAS");
        });
    server.verify();
  }

  @Test
  void answersCentralUnavailableWhenTheCentralAnswersOutsideTheContract() {
    server
        .expect(requestTo(BASE_URL + "/operacoes-portabilidade/" + PROTOCOL))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("[\"not-the-contract\"]"));

    assertThatThrownBy(() -> client.statusOf(PROTOCOL))
        .isInstanceOf(DomainError.CentralUnavailable.class)
        .hasMessageContaining("not the contract");
    server.verify();
  }

  @Test
  void replicatesTheOptInInTheCentral() {
    server
        .expect(requestTo(BASE_URL + "/autorizacoes"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("X-Signature", signatureOfTheAuthorization()))
        .andExpect(header("X-Timestamp", Long.toString(TIMESTAMP)))
        .andExpect(content().string(AUTHORIZATION_BODY))
        .andRespond(
            withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"autorizacaoId\":\"AUT-20261123-0001\"}"));

    client.register(new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"));

    server.verify();
  }

  @Test
  void acceptsTheReplicationOfAnOptInThatTheCentralAlreadyKnows() {
    server
        .expect(requestTo(BASE_URL + "/autorizacoes"))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"autorizacaoId\":\"AUT-20261123-0001\"}"));

    client.register(new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"));

    server.verify();
  }

  @Test
  void answersCentralUnavailableWhenTheCentralRefusesTheOptIn() {
    server
        .expect(requestTo(BASE_URL + "/autorizacoes"))
        .andRespond(
            withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"codigo\":\"ERRO_CENTRAL\",\"mensagem\":\"Indisponível.\"}"));

    assertThatThrownBy(() -> client.register(new BeneficiaryId("BEN-001"), new AgreementId("AGR-001")))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    server.verify();
  }

  @Test
  void answersCentralUnavailableWhenTheCentralDoesNotAnswerTheOptIn() {
    server
        .expect(requestTo(BASE_URL + "/autorizacoes"))
        .andRespond(
            request -> {
              throw new IOException("connection refused");
            });

    assertThatThrownBy(() -> client.register(new BeneficiaryId("BEN-001"), new AgreementId("AGR-001")))
        .isInstanceOf(DomainError.CentralUnavailable.class);
    server.verify();
  }

  private String signatureOfTheStatus() {
    return new HmacSigner(SECRET)
        .sign("GET", "/operacoes-portabilidade/" + PROTOCOL, TIMESTAMP, "");
  }

  private String signatureOfTheAgenda() {
    return new HmacSigner(SECRET).sign("GET", "/beneficiarios/BEN-001/agenda", TIMESTAMP, "");
  }

  private String signatureOfTheAuthorization() {
    return new HmacSigner(SECRET)
        .sign("POST", "/autorizacoes", TIMESTAMP, AUTHORIZATION_BODY);
  }

  private static PortabilityOperation operation() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.randomUUID()),
        new IdempotencyKey(UUID.fromString("11111111-2222-3333-4444-555555555555")),
        new BeneficiaryId("BEN-001"),
        new AgreementId("AGR-001"),
        List.of(
            new InstallmentSnapshot(
                new InstallmentId("PAR-001"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05")),
            new InstallmentSnapshot(
                new InstallmentId("PAR-002"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("300.00")),
                LocalDate.parse("2027-01-05"))),
        "ConsigPort",
        new ReceivingAccount("001", "1234", "56789-0"),
        Instant.ofEpochSecond(TIMESTAMP));
  }
}
