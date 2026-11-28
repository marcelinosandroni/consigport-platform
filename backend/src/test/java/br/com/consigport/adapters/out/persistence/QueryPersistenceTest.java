package br.com.consigport.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.application.audit.ListOperationEvents;
import br.com.consigport.application.portability.FindOperation;
import br.com.consigport.application.portability.ListOperations;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationDetail;
import br.com.consigport.domain.portability.OperationFilter;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.OperationSummary;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.Page;
import br.com.consigport.support.PostgreSqlTestSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The consultation of the operations and of their trail over PostgreSQL 16.
 *
 * <p>The test runs against the database of the stack: 25 operations are stored and the
 * consultation walks them one page at a time, with the filter of the status and the
 * chronological trail of the audit.
 */
@SpringBootTest
class QueryPersistenceTest extends PostgreSqlTestSupport {

  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final int TOTAL = 25;

  @Autowired private PortabilityOperationRepository operations;
  @Autowired private OperationEventRepository events;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    jdbc.execute(
        "TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void walksTheTwentyFiveOperationsOnePageAtATime() {
    storeOperations();

    ListOperations useCase = new ListOperations(operations);

    Page<OperationSummary> first = useCase.execute(new OperationFilter.All(), 0, 10);
    Page<OperationSummary> second = useCase.execute(new OperationFilter.All(), 1, 10);
    Page<OperationSummary> third = useCase.execute(new OperationFilter.All(), 2, 10);

    assertThat(first.items()).hasSize(10);
    assertThat(second.items()).hasSize(10);
    assertThat(third.items()).hasSize(5);
    assertThat(first.totalElements()).isEqualTo(TOTAL);
    assertThat(first.totalPages()).isEqualTo(3);
    assertThat(third.isLast()).isTrue();
  }

  @Test
  void neverRepeatsAnOperationBetweenTwoPages() {
    storeOperations();

    ListOperations useCase = new ListOperations(operations);

    List<UUID> first = ids(useCase.execute(new OperationFilter.All(), 0, 10));
    List<UUID> second = ids(useCase.execute(new OperationFilter.All(), 1, 10));

    assertThat(first).doesNotContainAnyElementsOf(second);
    assertThat(first).hasSize(10);
    assertThat(second).hasSize(10);
  }

  @Test
  void filtersByStatus() {
    storeOperations();

    ListOperations useCase = new ListOperations(operations);

    Page<OperationSummary> completed =
        useCase.execute(new OperationFilter.WithStatus(OperationStatus.COMPLETED), 0, 20);
    Page<OperationSummary> pending =
        useCase.execute(new OperationFilter.WithStatus(OperationStatus.PENDING), 0, 20);

    assertThat(completed.items()).hasSize(5);
    assertThat(completed.items())
        .allMatch(operation -> operation.status() == OperationStatus.COMPLETED);
    assertThat(pending.items()).hasSize(20);
    assertThat(completed.totalElements()).isEqualTo(5);
  }

  @Test
  void findsOneOperationWithItsWholeHistory() {
    storeOperations();
    OperationSummary stored = operations.list(new OperationFilter.All(), 0, 1).items().get(0);

    OperationDetail found = new FindOperation(operations).execute(stored.id());

    assertThat(found.id()).isEqualTo(stored.id());
    assertThat(found.protocol()).isEqualTo(stored.protocol());
    assertThat(found.totalAmount()).isEqualTo(stored.totalAmount());
    assertThat(found.updatedAt()).isNotNull();
  }

  @Test
  void listsTheTrailInChronologicalOrder() {
    storeOperations();
    OperationSummary stored = operations.list(new OperationFilter.All(), 0, 1).items().get(0);
    events.append(
        OperationEvent.record(
            stored.id(),
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            null,
            NOW));
    events.append(
        OperationEvent.record(
            stored.id(),
            OperationStatus.CREATED,
            OperationStatus.PENDING,
            EventType.OPERATION_SUBMITTED,
            "Central accepted the operation",
            null,
            NOW.plusSeconds(1)));

    List<OperationEvent> trail = new ListOperationEvents(operations, events).execute(stored.id());

    assertThat(trail).hasSize(2);
    assertThat(trail.get(0).eventType()).isEqualTo(EventType.OPERATION_CREATED);
    assertThat(trail.get(1).eventType()).isEqualTo(EventType.OPERATION_SUBMITTED);
    assertThat(trail.get(0).createdAt()).isBefore(trail.get(1).createdAt());
  }

  private void storeOperations() {
    for (int index = 0; index < TOTAL; index++) {
      operations.save(operation(index));
    }
  }

  private static List<UUID> ids(Page<OperationSummary> page) {
    return page.items().stream().map(summary -> summary.id().value()).toList();
  }

  private static PortabilityOperation operation(int index) {
    PortabilityOperation operation =
        PortabilityOperation.create(
                new PortabilityOperation.OperationId(
                    UUID.nameUUIDFromBytes(
                        ("operation-2.14-" + index)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))),
                new IdempotencyKey(
                    UUID.nameUUIDFromBytes(
                        ("key-2.14-" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
    if (index % 5 == 0) {
      return operation.confirm().complete(NOW.plusSeconds(index));
    }
    return operation;
  }
}
