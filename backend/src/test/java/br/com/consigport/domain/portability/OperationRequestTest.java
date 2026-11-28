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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the request of a portability operation and the comparison of idempotency.
 *
 * <p>The request is what the beneficiary chose: the installments, the creditor and the
 * account. The comparison of {@link OperationRequest#matches(PortabilityOperation)} is what
 * tells a replay from a different request under the same key: the order of the selected
 * installments does not change the request, the amounts and the dates do not belong to it.
 */
class OperationRequestTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-27T09:00:00Z");
  private static final String CREDITOR = "CREDOR-CESSIONARIO";
  private static final ReceivingAccount ACCOUNT = new ReceivingAccount("001", "1234", "56789-0");

  @Nested
  class InvariantsTest {

    @Test
    void keepsTheSelectionFrozen() {
      OperationRequest request = request();

      assertThatThrownBy(() -> request.installmentIds().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void trimsTheCreditor() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-001")),
              "  CREDOR-CESSIONARIO  ",
              ACCOUNT);

      assertThat(request.assigneeCreditor()).isEqualTo(CREDITOR);
    }

    @Test
    void refusesAMissingBeneficiary() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      null,
                      new AgreementId("AGR-001"),
                      List.of(new InstallmentId("PAR-001")),
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be null");
    }

    @Test
    void refusesAMissingAgreement() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      null,
                      List.of(new InstallmentId("PAR-001")),
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must not be null");
    }

    @Test
    void refusesAnEmptySelection() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(),
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentIds must have at least one element");
    }

    @Test
    void refusesAMissingSelection() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      null,
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentIds must have at least one element");
    }

    @Test
    void refusesAnInstallmentThatIsNotInTheSelection() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      Arrays.asList(new InstallmentId("PAR-001"), null),
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentIds must not contain null");
    }

    @Test
    void refusesARepeatedInstallment() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-001")),
                      CREDITOR,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentIds must not repeat an installment");
    }

    @Test
    void refusesABlankCreditor() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(new InstallmentId("PAR-001")),
                      "  ",
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("assigneeCreditor must not be blank");
    }

    @Test
    void refusesAMissingCreditor() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(new InstallmentId("PAR-001")),
                      null,
                      ACCOUNT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("assigneeCreditor must not be blank");
    }

    @Test
    void refusesAMissingAccount() {
      assertThatThrownBy(
              () ->
                  new OperationRequest(
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(new InstallmentId("PAR-001")),
                      CREDITOR,
                      null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("receivingAccount must not be null");
    }
  }

  @Nested
  class MatchesTest {

    @Test
    void recognizesTheSameRequest() {
      OperationRequest request = request();

      assertThat(request.matches(operation())).isTrue();
    }

    @Test
    void ignoresTheOrderOfTheInstallments() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-002"), new InstallmentId("PAR-001")),
              CREDITOR,
              ACCOUNT);

      assertThat(request.matches(operation())).isTrue();
    }

    @Test
    void refusesToCompareWithAMissingOperation() {
      assertThat(request().matches(null)).isFalse();
    }

    @Test
    void tellsAnotherBeneficiaryApart() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-002"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
              CREDITOR,
              ACCOUNT);

      assertThat(request.matches(operation())).isFalse();
    }

    @Test
    void tellsAnotherAgreementApart() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-002"),
              List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
              CREDITOR,
              ACCOUNT);

      assertThat(request.matches(operation())).isFalse();
    }

    @Test
    void tellsAnotherCreditorApart() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
              "OUTRO-CREDOR",
              ACCOUNT);

      assertThat(request.matches(operation())).isFalse();
    }

    @Test
    void tellsAnotherAccountApart() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
              CREDITOR,
              new ReceivingAccount("001", "1234", "99999-9"));

      assertThat(request.matches(operation())).isFalse();
    }

    @Test
    void tellsAnotherSelectionApart() {
      OperationRequest request =
          new OperationRequest(
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              List.of(new InstallmentId("PAR-001")),
              CREDITOR,
              ACCOUNT);

      assertThat(request.matches(operation())).isFalse();
    }
  }

  private static OperationRequest request() {
    return new OperationRequest(
        new BeneficiaryId("BEN-001"),
        new AgreementId("AGR-001"),
        List.of(new InstallmentId("PAR-001"), new InstallmentId("PAR-002")),
        CREDITOR,
        ACCOUNT);
  }

  private static PortabilityOperation operation() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(
            UUID.fromString("5e4d3c2b-1a09-4f8e-9d7c-6b5a4938271f")),
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
        CREDITOR,
        ACCOUNT,
        CREATED_AT);
  }
}
