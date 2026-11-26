package br.com.consigport.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.support.PostgreSqlTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The public edge of the start of a portability operation, over real HTTP.
 *
 * <p>The test runs against PostgreSQL 16 with the migration applied by Flyway: the
 * unique constraint of the idempotency key is what turns two concurrent requests into
 * one operation, so the race is only real against the database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PortabilityOperationResourceTest.AgendaStub.class)
class PortabilityOperationResourceTest extends PostgreSqlTestSupport {

  private static final String PATH = "/api/v1/portability-operations";

  private static final String BODY =
      """
      {
        "beneficiaryId": "BEN-001",
        "agreementId": "AGR-001",
        "installmentIds": ["PAR-001", "PAR-002"],
        "assigneeCreditor": "ConsigPort",
        "receivingAccount": {"bank": "001", "branch": "1234", "account": "56789-0"}
      }
      """;

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    // TRUNCATE does not fire the row trigger of the trail, so the append-only
    // guarantee survives the isolation of the tests.
    jdbc.execute("TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void createsTheOperationAndAnswersItAsAccepted() {
    ResponseEntity<String> response = post(UUID.randomUUID().toString(), BODY);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(response.getHeaders().getFirst("Idempotent-Replay")).isEqualTo("false");
    assertThat(response.getBody())
        .contains("\"status\":\"CREATED\"")
        .contains("\"protocol\":null")
        .contains("\"totalAmount\":750.00")
        .contains("\"operationId\":");    assertThat(
            jdbc.queryForObject("select count(*) from portability_operations", Integer.class))
        .isEqualTo(1);
    assertThat(jdbc.queryForObject("select count(*) from operation_events", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void freezesTheSnapshotOfTheInstallments() {
    ResponseEntity<String> response = post(UUID.randomUUID().toString(), BODY);

    String snapshot =
        jdbc.queryForObject(
            "select installments::text from portability_operations limit 1", String.class);
    assertThat(snapshot)
        .contains("PAR-001")
        .contains("PAR-002")
        .contains("450.00")
        .contains("2026-12-05");
  }

  @Test
  void answersTheExistingOperationWhenTheKeyIsRepeated() {
    String key = UUID.randomUUID().toString();

    ResponseEntity<String> first = post(key, BODY);
    ResponseEntity<String> second = post(key, BODY);

    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(second.getHeaders().getFirst("Idempotent-Replay")).isEqualTo("true");
    assertThat(second.getBody()).isEqualTo(first.getBody());
    assertThat(
            jdbc.queryForObject("select count(*) from portability_operations", Integer.class))
        .isEqualTo(1);
    assertThat(jdbc.queryForObject("select count(*) from operation_events", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void refusesTheKeyWhenThePayloadIsAnother() {
    String key = UUID.randomUUID().toString();
    post(key, BODY);
    String divergent =
        """
        {
          "beneficiaryId": "BEN-001",
          "agreementId": "AGR-001",
          "installmentIds": ["PAR-001"],
          "assigneeCreditor": "ConsigPort",
          "receivingAccount": {"bank": "001", "branch": "1234", "account": "56789-0"}
        }
        """;

    ResponseEntity<String> response = post(key, divergent);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody())
        .contains("\"code\":\"IDEMPOTENCY_CONFLICT\"")
        .contains("\"locale\":\"pt-BR\"");
  }

  @Test
  void refusesAClosedInstallment() {
    String body =
        """
        {
          "beneficiaryId": "BEN-001",
          "agreementId": "AGR-001",
          "installmentIds": ["PAR-003"],
          "assigneeCreditor": "ConsigPort",
          "receivingAccount": {"bank": "001", "branch": "1234", "account": "56789-0"}
        }
        """;

    ResponseEntity<String> response = post(UUID.randomUUID().toString(), body);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).contains("\"code\":\"INSTALLMENT_NOT_ELIGIBLE\"");
  }

  @Test
  void refusesARequestWithoutTheIdempotencyKey() {
    ResponseEntity<String> response = post(null, BODY);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).contains("\"code\":\"IDEMPOTENCY_KEY_REQUIRED\"");
  }

  @Test
  void refusesARequestWithoutTheBeneficiary() {
    String body =
        """
        {
          "agreementId": "AGR-001",
          "installmentIds": ["PAR-001"],
          "assigneeCreditor": "ConsigPort",
          "receivingAccount": {"bank": "001", "branch": "1234", "account": "56789-0"}
        }
        """;

    ResponseEntity<String> response = post(UUID.randomUUID().toString(), body);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .contains("\"code\":\"VALIDATION_ERROR\"")
        .contains("beneficiaryId");
  }

  @Test
  void answersOneOperationToTwoConcurrentRequests() throws Exception {
    String key = UUID.randomUUID().toString();
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(2);

    try {
      Future<ResponseEntity<String>> first =
          pool.submit(
              () -> {
                start.await();
                return post(key, BODY);
              });
      Future<ResponseEntity<String>> second =
          pool.submit(
              () -> {
                start.await();
                return post(key, BODY);
              });
      start.countDown();

      ResponseEntity<String> firstResponse = first.get();
      ResponseEntity<String> secondResponse = second.get();

      assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
      assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
      assertThat(secondResponse.getBody()).isEqualTo(firstResponse.getBody());
      assertThat(
              jdbc.queryForObject("select count(*) from portability_operations", Integer.class))
          .isEqualTo(1);
      assertThat(jdbc.queryForObject("select count(*) from operation_events", Integer.class))
          .isEqualTo(1);
    } finally {
      pool.shutdownNow();
    }
  }

  private ResponseEntity<String> post(String idempotencyKey, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (idempotencyKey != null) {
      headers.set("Idempotency-Key", idempotencyKey);
    }
    return rest.exchange(
        PATH, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
  }

  /**
   * Agenda of the beneficiary, served in the place of the Central.
   *
   * <p>The HTTP client of the Central arrives with task 2.10; until then the stub
   * answers the agenda that the operation start reads.
   */
  @TestConfiguration(proxyBeanMethods = false)
  static class AgendaStub {

    /**
     * The agenda of the beneficiary.
     *
     * @return the gateway of the agenda
     */
    @Bean
    @Primary
    AgendaGateway stubAgendaGateway() {
      return beneficiaryId ->
          new BeneficiaryAgenda(
              beneficiaryId,
              List.of(
                  new Installment(
                      new InstallmentId("PAR-001"),
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("450.00")),
                      LocalDate.parse("2026-12-05"),
                      InstallmentStatus.OPEN),
                  new Installment(
                      new InstallmentId("PAR-002"),
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("300.00")),
                      LocalDate.parse("2027-01-05"),
                      InstallmentStatus.OPEN),
                  new Installment(
                      new InstallmentId("PAR-003"),
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("100.00")),
                      LocalDate.parse("2027-02-05"),
                      InstallmentStatus.CLOSED)));
    }
  }
}
