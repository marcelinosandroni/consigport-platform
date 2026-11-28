package br.com.consigport.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.support.PostgreSqlTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The public edge of the consultation of the operations, over real HTTP.
 *
 * <p>The test runs against PostgreSQL 16: the pagination and the filter are two queries
 * over the rows, so they are only real against the database. The operations are stored
 * through the port of the domain, the way the worker and the API store them.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperationQueryResourceTest extends PostgreSqlTestSupport {

  private static final String PATH = "/api/v1/portability-operations";
  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final int TOTAL = 25;

  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PortabilityOperationRepository operations;

  @BeforeEach
  void clean() {
    jdbc.execute("TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void listsTheOperationsOnePageAtATime() throws Exception {
    storeOperations();

    ResponseEntity<String> response = get(PATH + "?page=0&size=10");
    JsonNode body = mapper.readTree(response.getBody());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(body.get("page").asInt()).isZero();
    assertThat(body.get("size").asInt()).isEqualTo(10);
    assertThat(body.get("totalElements").asInt()).isEqualTo(TOTAL);
    assertThat(body.get("totalPages").asInt()).isEqualTo(3);
    assertThat(body.get("items").size()).isEqualTo(10);
  }

  @Test
  void walksThePagesWithoutRepeatingAnOperation() throws Exception {
    storeOperations();

    JsonNode first = mapper.readTree(get(PATH + "?page=0&size=10").getBody());
    JsonNode second = mapper.readTree(get(PATH + "?page=1&size=10").getBody());
    JsonNode third = mapper.readTree(get(PATH + "?page=2&size=10").getBody());

    assertThat(second.get("items").size()).isEqualTo(10);
    assertThat(third.get("items").size()).isEqualTo(5);
    assertThat(ids(first)).doesNotContainAnyElementsOf(ids(second));
    assertThat(ids(second)).doesNotContainAnyElementsOf(ids(third));
  }

  @Test
  void filtersTheListByStatus() throws Exception {
    storeOperations();

    JsonNode pending = mapper.readTree(get(PATH + "?status=PENDING&size=50").getBody());
    JsonNode completed = mapper.readTree(get(PATH + "?status=COMPLETED&size=50").getBody());

    assertThat(pending.get("totalElements").asInt()).isEqualTo(20);
    assertThat(pending.get("items").size()).isEqualTo(20);
    assertThat(completed.get("totalElements").asInt()).isEqualTo(5);
    assertThat(completed.get("items").size()).isEqualTo(5);
  }

  @Test
  void answersTheSummaryOfTheOperationInTheList() throws Exception {
    storeOperations();

    JsonNode body = mapper.readTree(get(PATH + "?status=PENDING&size=1").getBody());
    JsonNode item = body.get("items").get(0);

    assertThat(item.get("operationId").asText()).isNotBlank();
    assertThat(item.get("beneficiaryId").asText()).isEqualTo("BEN-001");
    assertThat(item.get("status").asText()).isEqualTo("PENDING");
    assertThat(item.get("totalAmount").asDouble()).isEqualTo(450.00);
    assertThat(item.get("createdAt").asText()).isNotBlank();
    assertThat(item.get("updatedAt").asText()).isNotBlank();
  }

  @Test
  void answersTheDetailOfOneOperation() throws Exception {
    PortabilityOperation stored = storeOperation(0, true);
    jdbc.update(
        "update portability_operations set updated_at = ? where id = ?",
        java.sql.Timestamp.from(Instant.parse("2026-11-27T12:30:00Z")),
        stored.id().value());

    ResponseEntity<String> response = get(PATH + "/" + stored.id().value());
    JsonNode body = mapper.readTree(response.getBody());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(body.get("operationId").asText()).isEqualTo(stored.id().value().toString());
    assertThat(body.get("status").asText()).isEqualTo("COMPLETED");
    assertThat(body.get("agreementId").asText()).isEqualTo("AGR-001");
    assertThat(body.get("installments").size()).isEqualTo(1);
    assertThat(body.get("installments").get(0).get("installmentId").asText())
        .isEqualTo("PAR-001");
    assertThat(body.get("assigneeCreditor").asText()).isEqualTo("ConsigPort");
    assertThat(body.get("receivingAccount").get("account").asText()).isEqualTo("56789-0");
    assertThat(body.get("rejectionReason").isNull()).isTrue();
    assertThat(body.get("retryCount").asInt()).isZero();
    assertThat(body.get("accountUpdatedAt").asText()).isNotBlank();
    assertThat(body.get("updatedAt").asText()).startsWith("2026-11-27T12:30:00");
  }

  @Test
  void answersNotFoundWhenTheOperationDoesNotExist() {
    ResponseEntity<String> response = get(PATH + "/" + UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("\"code\":\"OPERATION_NOT_FOUND\"");
  }

  @Test
  void refusesAPageOutsideTheAcceptedRange() {
    ResponseEntity<String> response = get(PATH + "?page=-1");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).contains("\"code\":\"VALIDATION_ERROR\"");
  }

  @Test
  void refusesASizeOutsideTheAcceptedRange() {
    ResponseEntity<String> response = get(PATH + "?size=101");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).contains("\"code\":\"VALIDATION_ERROR\"");
  }

  @Test
  void refusesAStatusThatDoesNotExist() {
    ResponseEntity<String> response = get(PATH + "?status=UNKNOWN");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).contains("\"code\":\"VALIDATION_ERROR\"");
  }

  private ResponseEntity<String> get(String path) {
    return rest.exchange(path, HttpMethod.GET, null, String.class);
  }

  private static List<String> ids(JsonNode page) {
    List<String> ids = new java.util.ArrayList<>();
    page.get("items").forEach(item -> ids.add(item.get("operationId").asText()));
    return ids;
  }

  private void storeOperations() {
    for (int index = 0; index < TOTAL; index++) {
      storeOperation(index, index % 5 == 0);
    }
  }

  private PortabilityOperation storeOperation(int index, boolean completed) {
    PortabilityOperation operation =
        PortabilityOperation.create(
                new PortabilityOperation.OperationId(
                    UUID.nameUUIDFromBytes(
                        ("operation-query-" + index)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))),
                new IdempotencyKey(
                    UUID.nameUUIDFromBytes(
                        ("key-query-" + index)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))),
                new BeneficiaryId("BEN-001"),
                new AgreementId("AGR-001"),
                List.of(
                    new InstallmentSnapshot(
                        new InstallmentId("PAR-001"),
                        new AgreementId("AGR-001"),
                        new Money(new BigDecimal("450.00")),
                        LocalDate.parse("2026-12-05"))),
                "ConsigPort",
                new ReceivingAccount("001", "1234", "56789-0"),
                NOW.plusSeconds(index))
            .submit("PRT-20261127-" + String.format("%04d", index));
    if (completed) {
      operation = operation.confirm().complete(NOW.plusSeconds(index));
    }
    operations.save(operation);
    return operation;
  }
}
