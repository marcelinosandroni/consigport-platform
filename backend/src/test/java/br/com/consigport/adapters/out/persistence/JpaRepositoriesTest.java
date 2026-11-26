package br.com.consigport.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.authorization.Authorization;
import br.com.consigport.domain.authorization.AuthorizationAlreadyExistsException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
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
 * Round trip of the aggregates over the schema V1.
 *
 * <p>The test runs against PostgreSQL 16 with the migration applied by Flyway and the
 * schema validated by Hibernate: the mapping and the DDL are proven to agree, field by
 * field, in both directions.
 */
@SpringBootTest
class JpaRepositoriesTest extends PostgreSqlTestSupport {

  private static final Instant NOW = Instant.parse("2026-11-26T10:15:30Z");

  @Autowired private JpaAuthorizationRepository authorizations;
  @Autowired private JpaPortabilityOperationRepository operations;
  @Autowired private JpaOperationEventRepository events;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    // TRUNCATE does not fire the row trigger of the trail, so the append-only
    // guarantee survives the isolation of the tests.
    jdbc.execute("TRUNCATE TABLE operation_events, portability_operations, authorizations");
  }

  @Test
  void storesAndFindsTheAuthorizationOfThePair() {
    Authorization authorization =
        Authorization.register(
            new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"), NOW);

    authorizations.save(authorization);

    assertThat(
            authorizations.findByBeneficiaryAndAgreement(
                new BeneficiaryId("BEN-001"), new AgreementId("AGR-001")))
        .contains(authorization);
  }

  @Test
  void answersEmptyForAnUnknownPair() {
    assertThat(
            authorizations.findByBeneficiaryAndAgreement(
                new BeneficiaryId("BEN-404"), new AgreementId("AGR-404")))
        .isEmpty();
  }

  @Test
  void refusesASecondAuthorizationOfTheSamePair() {
    Authorization authorization =
        Authorization.register(
            new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"), NOW);
    authorizations.save(authorization);

    Authorization repeated =
        Authorization.register(
            new BeneficiaryId("BEN-001"), new AgreementId("AGR-001"), NOW);

    assertThatThrownBy(() -> authorizations.save(repeated))
        .isInstanceOf(AuthorizationAlreadyExistsException.class);
  }

  @Test
  void storesAndFindsTheOperationByEveryKey() {
    PortabilityOperation operation = sampleOperation();

    operations.save(operation);

    assertThat(operations.findById(operation.id())).contains(operation);
    assertThat(operations.findByIdempotencyKey(operation.idempotencyKey()))
        .contains(operation);
  }

  @Test
  void answersEmptyForAnUnknownKey() {
    operations.save(sampleOperation());

    assertThat(operations.findByIdempotencyKey(new IdempotencyKey(UUID.randomUUID())))
        .isEmpty();
    assertThat(operations.findByProtocol("PROTO-404")).isEmpty();
  }

  @Test
  void keepsTheSnapshotOfTheInstallments() {
    PortabilityOperation operation = sampleOperation();

    operations.save(operation);

    PortabilityOperation stored = operations.findById(operation.id()).orElseThrow();
    assertThat(stored.installments())
        .extracting(
            snapshot ->
                snapshot.installmentId().value()
                    + "|"
                    + snapshot.agreementId().value()
                    + "|"
                    + snapshot.amount().amount()
                    + "|"
                    + snapshot.discountDate())
        .containsExactly(
            "PAR-001|AGR-001|450.00|2026-12-05", "PAR-002|AGR-001|300.00|2027-01-05");
    assertThat(stored.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
    assertThat(stored.receivingAccount())
        .isEqualTo(new ReceivingAccount("001", "1234", "56789-0"));
  }

  @Test
  void writesTheTransitionOfTheOperation() {
    PortabilityOperation operation = sampleOperation();
    operations.save(operation);

    PortabilityOperation submitted = operation.submit("PROTO-001");
    operations.save(submitted);

    PortabilityOperation stored = operations.findById(operation.id()).orElseThrow();
    assertThat(stored.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(stored.protocol()).isEqualTo("PROTO-001");
    assertThat(stored.retryCount()).isZero();
    assertThat(operations.findByProtocol("PROTO-001")).contains(stored);
  }

  @Test
  void appendsTheEventsOfTheTrail() {
    PortabilityOperation operation = sampleOperation();
    operations.save(operation);

    OperationEvent created =
        OperationEvent.record(
            operation.id(),
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            null,
            NOW);
    OperationEvent submitted =
        OperationEvent.record(
            operation.id(),
            OperationStatus.CREATED,
            OperationStatus.PENDING,
            EventType.OPERATION_SUBMITTED,
            "Operation submitted",
            "{\"status\":\"ACEITA\"}",
            NOW.plusSeconds(60));

    OperationEvent storedCreated = events.append(created);
    OperationEvent storedSubmitted = events.append(submitted);

    assertThat(storedCreated.eventId()).isNotNull();
    assertThat(storedCreated.previousStatus()).isNull();
    assertThat(events.findByOperationId(operation.id()))
        .extracting(OperationEvent::eventType)
        .containsExactly(EventType.OPERATION_CREATED, EventType.OPERATION_SUBMITTED);
    assertThat(events.findByOperationId(operation.id()).get(1).rawPayload())
        .isEqualTo("{\"status\":\"ACEITA\"}");
  }

  @Test
  void keepsTheRawPayloadByteForByte() {
    PortabilityOperation operation = sampleOperation();
    operations.save(operation);

    String rawPayload = "{\"status\":\"ACEITA\",\"valor\":450.00}";
    events.append(
        OperationEvent.record(
            operation.id(),
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            rawPayload,
            NOW));
    events.append(
        OperationEvent.record(
            operation.id(),
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            "plain body without json",
            NOW));

    assertThat(events.findByOperationId(operation.id()))
        .extracting(OperationEvent::rawPayload)
        .containsExactly(rawPayload, "plain body without json");
  }

  @Test
  void refusesAnEventOfAnUnknownOperation() {
    OperationEvent orphan =
        OperationEvent.record(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            null,
            NOW);

    assertThatThrownBy(() -> events.append(orphan))
        .hasMessageContaining("operation_events");
  }

  private static PortabilityOperation sampleOperation() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.randomUUID()),
        new IdempotencyKey(UUID.randomUUID()),
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
        "CREDOR-CESSIONARIO",
        new ReceivingAccount("001", "1234", "56789-0"),
        NOW);
  }
}
