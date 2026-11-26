package br.com.consigport.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.support.PostgreSqlTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Constraints of the schema V1 (ARCHITECTURE.md §6).
 *
 * <p>The test runs against PostgreSQL 16 with the migration applied by Flyway: the
 * status is restricted by a check constraint, the snapshot of the installments is a
 * non-empty JSONB array and the trail of events rejects any update or delete.
 */
@SpringBootTest
class PersistenceConstraintsTest extends PostgreSqlTestSupport {

  private static final java.sql.Timestamp NOW =
      java.sql.Timestamp.from(java.time.Instant.parse("2026-11-26T10:15:30Z"));

  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    // TRUNCATE does not fire the row trigger of the trail, so the append-only
    // guarantee survives the isolation of the tests.
    jdbc.execute(
        "TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void appliesTheMigrationOfTheSchema() {
    assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void refusesAStatusOutsideTheStateMachine() {
    assertThatThrownBy(() -> insertOperation("CONFIRMADA"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void acceptsEveryStatusOfTheStateMachine() {
    for (String status :
        new String[] {
          "CREATED", "PENDING", "CONFIRMED", "COMPLETED", "REJECTED", "INTEGRATION_ERROR"
        }) {
      insertOperation(status);
    }

    assertThat(
            jdbc.queryForObject(
                "select count(*) from portability_operations", Integer.class))
        .isEqualTo(6);
  }

  @Test
  void refusesAnEmptySnapshotOfInstallments() {
    assertThatThrownBy(() -> insertOperationWithInstallments("[]"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void refusesASnapshotThatIsNotAnArray() {
    assertThatThrownBy(() -> insertOperationWithInstallments("{\"parcelaId\":\"PAR-001\"}"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void refusesANegativeTotal() {
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "insert into portability_operations (id, idempotency_key, beneficiary_id,"
                        + " installments, assignee_creditor, receiving_bank, receiving_branch,"
                        + " receiving_account, total_amount, status, retry_count, created_at,"
                        + " updated_at) values (?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(),
                    UUID.randomUUID().toString(),
                    "BEN-001",
                    "[{\"installmentId\":\"PAR-001\"}]",
                    "CREDOR-CESSIONARIO",
                    "001",
                    "1234",
                    "56789-0",
                    -1,
                    "CREATED",
                    0,
                    NOW,
                    NOW))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void refusesTwoAuthorizationsOfTheSamePair() {
    insertAuthorization("BEN-001", "AGR-001");

    assertThatThrownBy(() -> insertAuthorization("BEN-001", "AGR-001"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void refusesTwoOperationsWithTheSameIdempotencyKey() {
    String key = UUID.randomUUID().toString();
    insertOperationWithKey(key, "CREATED");

    assertThatThrownBy(() -> insertOperationWithKey(key, "CREATED"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void refusesToUpdateAnEventOfTheTrail() {
    UUID operationId = insertOperationRow("CREATED");
    Long eventId = insertEvent(operationId);

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "update operation_events set details = ? where id = ?", "changed", eventId))
        .isInstanceOf(DataAccessException.class)
        .hasMessageContaining("operation_events is append-only");
  }

  @Test
  void refusesToDeleteAnEventOfTheTrail() {
    UUID operationId = insertOperationRow("CREATED");
    Long eventId = insertEvent(operationId);

    assertThatThrownBy(
            () -> jdbc.update("delete from operation_events where id = ?", eventId))
        .isInstanceOf(DataAccessException.class)
        .hasMessageContaining("operation_events is append-only");
  }

  @Test
  void appendsEventsWithoutChangingTheTrail() {
    UUID operationId = insertOperationRow("CREATED");
    insertEvent(operationId);
    insertEvent(operationId);

    assertThat(jdbc.queryForObject("select count(*) from operation_events", Integer.class))
        .isEqualTo(2);
  }

  private void insertOperation(String status) {
    insertOperationWithKey(UUID.randomUUID().toString(), status);
  }

  private void insertOperationWithKey(String idempotencyKey, String status) {
    insertOperation(idempotencyKey, status, "[{\"installmentId\":\"PAR-001\"}]");
  }

  private void insertOperationWithInstallments(String installments) {
    insertOperation(UUID.randomUUID().toString(), "CREATED", installments);
  }

  private UUID insertOperationRow(String status) {
    return insertOperation(UUID.randomUUID().toString(), status, "[{\"installmentId\":\"PAR-001\"}]");
  }

  private UUID insertOperation(String idempotencyKey, String status, String installments) {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into portability_operations (id, idempotency_key, beneficiary_id,"
            + " installments, assignee_creditor, receiving_bank, receiving_branch,"
            + " receiving_account, total_amount, status, retry_count, created_at, updated_at)"
            + " values (?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        idempotencyKey,
        "BEN-001",
        installments,
        "CREDOR-CESSIONARIO",
        "001",
        "1234",
        "56789-0",
        450,
        status,
        0,
        NOW,
        NOW);
    return id;
  }

  private void insertAuthorization(String beneficiaryId, String agreementId) {
    jdbc.update(
        "insert into authorizations (id, beneficiary_id, agreement_id, created_at)"
            + " values (?, ?, ?, ?)",
        UUID.randomUUID(),
        beneficiaryId,
        agreementId,
        NOW);
  }

  private Long insertEvent(UUID operationId) {
    jdbc.update(
        "insert into operation_events (operation_id, previous_status, new_status, event_type,"
            + " details, created_at) values (?, ?, ?, ?, ?, ?)",
        operationId,
        null,
        "CREATED",
        "OPERATION_CREATED",
        "Operation created",
        NOW);
    return jdbc.queryForObject(
        "select max(id) from operation_events where operation_id = ?", Long.class, operationId);
  }
}
