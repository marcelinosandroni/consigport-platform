package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Invariants and state machine of the portability aggregate.
 *
 * <p>The transitions are checked against the table of {@code ARCHITECTURE.md} §7: every
 * combination of current status and trigger is exercised, and only the rows of the table
 * are accepted. The terminal statuses never leave their state.
 */
class PortabilityOperationTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-26T10:15:30Z");
  private static final Instant LATER = Instant.parse("2026-11-26T10:16:30Z");
  private static final String PROTOCOL = "PRT-20261126-0001";

  private static PortabilityOperation newOperation() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.fromString("3f0b0d3e-8a6d-4a8f-9b2f-6f1d2c3b4a50")),
        IdempotencyKey.parse("6f1a1b4e-0f9d-4d0a-9f37-3a1d1a2b3c4d"),
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
                LocalDate.parse("2026-12-20"))),
        "CREDOR-CESSIONARIO",
        new ReceivingAccount("001", "1234", "56789-0"),
        CREATED_AT);
  }

  static Stream<Arguments> transitions() {
    return Stream.of(
        Arguments.of(OperationStatus.CREATED, Trigger.SUBMIT, true, OperationStatus.PENDING),
        Arguments.of(OperationStatus.CREATED, Trigger.POLL_ATTEMPT, false, null),
        Arguments.of(OperationStatus.CREATED, Trigger.CONFIRM, false, null),
        Arguments.of(OperationStatus.CREATED, Trigger.REJECT, false, null),
        Arguments.of(
            OperationStatus.CREATED,
            Trigger.INTEGRATION_FAILURE,
            true,
            OperationStatus.INTEGRATION_ERROR),
        Arguments.of(OperationStatus.CREATED, Trigger.COMPLETE, false, null),
        Arguments.of(OperationStatus.PENDING, Trigger.SUBMIT, false, null),
        Arguments.of(OperationStatus.PENDING, Trigger.POLL_ATTEMPT, true, OperationStatus.PENDING),
        Arguments.of(OperationStatus.PENDING, Trigger.CONFIRM, true, OperationStatus.CONFIRMED),
        Arguments.of(OperationStatus.PENDING, Trigger.REJECT, true, OperationStatus.REJECTED),
        Arguments.of(
            OperationStatus.PENDING, Trigger.INTEGRATION_FAILURE, true, OperationStatus.INTEGRATION_ERROR),
        Arguments.of(OperationStatus.PENDING, Trigger.COMPLETE, false, null),
        Arguments.of(OperationStatus.INTEGRATION_ERROR, Trigger.SUBMIT, true, OperationStatus.PENDING),
        Arguments.of(
            OperationStatus.INTEGRATION_ERROR, Trigger.POLL_ATTEMPT, true, OperationStatus.PENDING),
        Arguments.of(
            OperationStatus.INTEGRATION_ERROR, Trigger.CONFIRM, true, OperationStatus.CONFIRMED),
        Arguments.of(
            OperationStatus.INTEGRATION_ERROR, Trigger.REJECT, true, OperationStatus.REJECTED),
        Arguments.of(
            OperationStatus.INTEGRATION_ERROR, Trigger.INTEGRATION_FAILURE, false, null),
        Arguments.of(OperationStatus.INTEGRATION_ERROR, Trigger.COMPLETE, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.SUBMIT, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.POLL_ATTEMPT, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.CONFIRM, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.REJECT, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.INTEGRATION_FAILURE, false, null),
        Arguments.of(OperationStatus.CONFIRMED, Trigger.COMPLETE, true, OperationStatus.COMPLETED),
        Arguments.of(OperationStatus.COMPLETED, Trigger.SUBMIT, false, null),
        Arguments.of(OperationStatus.COMPLETED, Trigger.POLL_ATTEMPT, false, null),
        Arguments.of(OperationStatus.COMPLETED, Trigger.CONFIRM, false, null),
        Arguments.of(OperationStatus.COMPLETED, Trigger.REJECT, false, null),
        Arguments.of(OperationStatus.COMPLETED, Trigger.INTEGRATION_FAILURE, false, null),
        Arguments.of(OperationStatus.COMPLETED, Trigger.COMPLETE, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.SUBMIT, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.POLL_ATTEMPT, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.CONFIRM, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.REJECT, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.INTEGRATION_FAILURE, false, null),
        Arguments.of(OperationStatus.REJECTED, Trigger.COMPLETE, false, null));
  }

  @ParameterizedTest(name = "{0} + {1} → allowed: {2}")
  @MethodSource("transitions")
  void followsOnlyTheTransitionsOfTheStateMachine(
      OperationStatus from, Trigger trigger, boolean allowed, OperationStatus expectedStatus) {
    PortabilityOperation operation = operationIn(from);

    if (allowed) {
      PortabilityOperation moved = trigger.apply(operation);

      assertThat(moved.status()).isEqualTo(expectedStatus);
    } else {
      assertThatThrownBy(() -> trigger.apply(operation))
          .isInstanceOf(PortabilityOperation.InvalidTransitionException.class);
    }
  }

  @Test
  void keepsTheOperationImmutableOnEveryTransition() {
    PortabilityOperation operation = newOperation();

    PortabilityOperation submitted = operation.submit(PROTOCOL);

    assertThat(operation.status()).isEqualTo(OperationStatus.CREATED);
    assertThat(operation.protocol()).isNull();
    assertThat(submitted.status()).isEqualTo(OperationStatus.PENDING);
    assertThat(submitted.protocol()).isEqualTo(PROTOCOL);
    assertThat(submitted.id()).isEqualTo(operation.id());
    assertThat(submitted.idempotencyKey()).isEqualTo(operation.idempotencyKey());
    assertThat(submitted.installments()).isEqualTo(operation.installments());
    assertThat(submitted.totalAmount()).isEqualTo(operation.totalAmount());
    assertThat(submitted.createdAt()).isEqualTo(operation.createdAt());
  }

  @Test
  void neverLeavesATerminalStatus() {
    assertThat(OperationStatus.COMPLETED.isTerminal()).isTrue();
    assertThat(OperationStatus.REJECTED.isTerminal()).isTrue();
    assertThat(OperationStatus.CREATED.isTerminal()).isFalse();
    assertThat(OperationStatus.PENDING.isTerminal()).isFalse();
    assertThat(OperationStatus.CONFIRMED.isTerminal()).isFalse();
    assertThat(OperationStatus.INTEGRATION_ERROR.isTerminal()).isFalse();
  }

  @Test
  void countsEveryFailedIntegration() {
    PortabilityOperation failed = newOperation().registerIntegrationFailure(LATER);

    assertThat(failed.retryCount()).isEqualTo(1);
    assertThat(failed.nextPollAt()).isEqualTo(LATER);
    assertThat(failed.status()).isEqualTo(OperationStatus.INTEGRATION_ERROR);
  }

  @Test
  void keepsTheProtocolWhenTheIntegrationFailsAfterTheAcceptance() {
    PortabilityOperation failed = newOperation().submit(PROTOCOL).registerIntegrationFailure(LATER);

    assertThat(failed.protocol()).isEqualTo(PROTOCOL);
  }

  @Test
  void refusesToSubmitWithoutAProtocol() {
    assertThatThrownBy(() -> newOperation().submit(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("protocol must not be blank");
  }

  @Test
  void refusesToRejectWithoutAReason() {
    PortabilityOperation pending = newOperation().submit(PROTOCOL);

    assertThatThrownBy(() -> pending.reject(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("rejectionReason must not be null");
  }

  @Test
  void namesTheRefusedTransition() {
    PortabilityOperation completed =
        newOperation().submit(PROTOCOL).confirm().complete(LATER);

    assertThatThrownBy(() -> completed.submit(PROTOCOL))
        .isInstanceOf(PortabilityOperation.InvalidTransitionException.class)
        .hasMessage("operation cannot move from COMPLETED to PENDING")
        .extracting(exception -> ((PortabilityOperation.InvalidTransitionException) exception).from())
        .isEqualTo(OperationStatus.COMPLETED);
  }

  @Nested
  class Creation {

    @Test
    void startsInCreatedWithoutAProtocol() {
      PortabilityOperation operation = newOperation();

      assertThat(operation.status()).isEqualTo(OperationStatus.CREATED);
      assertThat(operation.protocol()).isNull();
      assertThat(operation.rejectionReason()).isNull();
      assertThat(operation.retryCount()).isZero();
      assertThat(operation.accountUpdatedAt()).isNull();
      assertThat(operation.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void totalsTheInstallments() {
      assertThat(newOperation().totalAmount().amount()).isEqualTo(new BigDecimal("750.00"));
    }

    @Test
    void freezesTheSnapshot() {
      PortabilityOperation operation = newOperation();

      assertThatThrownBy(() -> operation.installments().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void refusesAnEmptySnapshot() {
      assertThatThrownBy(
              () ->
                  PortabilityOperation.create(
                      new PortabilityOperation.OperationId(UUID.randomUUID()),
                      IdempotencyKey.parse("6f1a1b4e-0f9d-4d0a-9f37-3a1d1a2b3c4d"),
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installments must have at least one element");
    }

    @Test
    void refusesATotalThatIsNotTheSumOfTheSnapshot() {
      PortabilityOperation operation = newOperation();

      assertThatThrownBy(
              () ->
                  new PortabilityOperation(
                      operation.id(),
                      operation.idempotencyKey(),
                      operation.beneficiaryId(),
                      operation.agreementId(),
                      operation.installments(),
                      operation.assigneeCreditor(),
                      operation.receivingAccount(),
                      new Money(new BigDecimal("999.00")),
                      operation.status(),
                      operation.protocol(),
                      operation.rejectionReason(),
                      operation.retryCount(),
                      operation.nextPollAt(),
                      operation.accountUpdatedAt(),
                      operation.createdAt()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("totalAmount must be the sum of the installments");
    }

    @Test
    void refusesABlankAssigneeCreditor() {
      assertThatThrownBy(
              () ->
                  PortabilityOperation.create(
                      new PortabilityOperation.OperationId(UUID.randomUUID()),
                      IdempotencyKey.parse("6f1a1b4e-0f9d-4d0a-9f37-3a1d1a2b3c4d"),
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(
                          new InstallmentSnapshot(
                              new InstallmentId("PAR-001"),
                              new AgreementId("AGR-001"),
                              new Money(new BigDecimal("450.00")),
                              LocalDate.parse("2026-12-05"))),
                      "  ",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("assigneeCreditor must not be blank");
    }
  }

  @Nested
  class OperationIdTest {

    private static final String UUID_TEXT = "3f0b0d3e-8a6d-4a8f-9b2f-6f1d2c3b4a50";

    @Test
    void parsesAUuid() {
      assertThat(PortabilityOperation.OperationId.parse(UUID_TEXT).value().toString())
          .isEqualTo(UUID_TEXT);
    }

    @Test
    void printsTheCanonicalText() {
      assertThat(PortabilityOperation.OperationId.parse(UUID_TEXT)).hasToString(UUID_TEXT);
    }

    @Test
    void rejectsBlankIdentities() {
      assertThatThrownBy(() -> PortabilityOperation.OperationId.parse(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("operationId must not be blank");
    }

    @Test
    void rejectsIdentitiesThatAreNotUuids() {
      assertThatThrownBy(() -> PortabilityOperation.OperationId.parse("not-a-uuid"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("operationId must be a UUID");
    }
  }

  private static PortabilityOperation operationIn(OperationStatus status) {
    PortabilityOperation operation = newOperation();
    return switch (status) {
      case CREATED -> operation;
      case PENDING -> operation.submit(PROTOCOL);
      case CONFIRMED -> operation.submit(PROTOCOL).confirm();
      case COMPLETED -> operation.submit(PROTOCOL).confirm().complete(LATER);
      case REJECTED -> operation.submit(PROTOCOL).reject(new RejectionReason.MarginInsufficient());
      case INTEGRATION_ERROR -> operation.registerIntegrationFailure(LATER);
    };
  }

  /** Trigger of the state machine, as named in the table of ARCHITECTURE.md §7. */
  private enum Trigger {
    SUBMIT,
    POLL_ATTEMPT,
    CONFIRM,
    REJECT,
    INTEGRATION_FAILURE,
    COMPLETE;

    PortabilityOperation apply(PortabilityOperation operation) {
      return switch (this) {
        case SUBMIT -> operation.submit(PROTOCOL);
        case POLL_ATTEMPT -> operation.pollAttempt(LATER);
        case CONFIRM -> operation.confirm();
        case REJECT -> operation.reject(new RejectionReason.MarginInsufficient());
        case INTEGRATION_FAILURE -> operation.registerIntegrationFailure(LATER);
        case COMPLETE -> operation.complete(LATER);
      };
    }
  }
}
