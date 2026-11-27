package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the read models of the consultation of the operations.
 *
 * <p>The summary and the detail are what the public API shows: they are built from the
 * row of the storage, so every field they carry may be absent. The constructor is the
 * place where the domain refuses an inconsistent read model, and each refusal is part of
 * the contract of the model.
 */
class OperationReadModelsTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-27T09:00:00Z");
  private static final Instant UPDATED_AT = Instant.parse("2026-11-27T10:00:00Z");
  private static final PortabilityOperation.OperationId OPERATION_ID =
      new PortabilityOperation.OperationId(
          UUID.fromString("9a8b7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d"));

  @Nested
  class SummaryTest {

    @Test
    void carriesTheIdentityAndTheLifeOfTheOperation() {
      OperationSummary summary = summary();

      assertThat(summary.id()).isEqualTo(OPERATION_ID);
      assertThat(summary.protocol()).isEqualTo("PRT-20261127-0001");
      assertThat(summary.beneficiaryId()).isEqualTo(new BeneficiaryId("BEN-001"));
      assertThat(summary.status()).isEqualTo(OperationStatus.PENDING);
      assertThat(summary.totalAmount()).isEqualTo(new Money(new BigDecimal("750.00")));
      assertThat(summary.createdAt()).isEqualTo(CREATED_AT);
      assertThat(summary.updatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void refusesAMissingIdentity() {
      assertThatThrownBy(() -> summary(null, "PRT-20261127-0001", new BeneficiaryId("BEN-001")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("operationId must not be null");
    }

    @Test
    void refusesAMissingBeneficiary() {
      assertThatThrownBy(() -> summary(OPERATION_ID, "PRT-20261127-0001", null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be null");
    }

    @Test
    void refusesAMissingStatus() {
      assertThatThrownBy(
              () ->
                  new OperationSummary(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      null,
                      new Money(new BigDecimal("750.00")),
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("status must not be null");
    }

    @Test
    void refusesAMissingTotal() {
      assertThatThrownBy(
              () ->
                  new OperationSummary(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      OperationStatus.PENDING,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("totalAmount must not be null");
    }

    @Test
    void refusesAMissingCreationInstant() {
      assertThatThrownBy(
              () ->
                  new OperationSummary(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      OperationStatus.PENDING,
                      new Money(new BigDecimal("750.00")),
                      null,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("createdAt must not be null");
    }

    @Test
    void refusesAMissingUpdateInstant() {
      assertThatThrownBy(
              () ->
                  new OperationSummary(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      OperationStatus.PENDING,
                      new Money(new BigDecimal("750.00")),
                      CREATED_AT,
                      null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("updatedAt must not be null");
    }
  }

  @Nested
  class DetailTest {

    @Test
    void carriesTheSnapshotAndTheAccount() {
      OperationDetail detail = detail();

      assertThat(detail.id()).isEqualTo(OPERATION_ID);
      assertThat(detail.agreementId()).isEqualTo(new AgreementId("AGR-001"));
      assertThat(detail.installments()).hasSize(2);
      assertThat(detail.assigneeCreditor()).isEqualTo("CREDOR-CESSIONARIO");
      assertThat(detail.receivingAccount()).isEqualTo(new ReceivingAccount("001", "1234", "56789-0"));
      assertThat(detail.retryCount()).isZero();
      assertThat(detail.rejectionReason()).isNull();
      assertThat(detail.accountUpdatedAt()).isNull();
    }

    @Test
    void freezesTheSnapshotOfTheInstallments() {
      OperationDetail detail = detail();

      assertThatThrownBy(() -> detail.installments().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void carriesTheReasonOfARejectedOperation() {
      OperationDetail detail =
          new OperationDetail(
              OPERATION_ID,
              "PRT-20261127-0001",
              new BeneficiaryId("BEN-001"),
              new AgreementId("AGR-001"),
              installments(),
              "CREDOR-CESSIONARIO",
              new ReceivingAccount("001", "1234", "56789-0"),
              new Money(new BigDecimal("750.00")),
              OperationStatus.REJECTED,
              new RejectionReason.MarginInsufficient(),
              0,
              null,
              CREATED_AT,
              UPDATED_AT);

      assertThat(detail.rejectionReason())
          .isInstanceOf(RejectionReason.MarginInsufficient.class);
    }

    @Test
    void refusesAMissingCreditor() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      null,
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("assigneeCreditor must not be blank");
    }

    @Test
    void refusesAMissingIdentity() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      null,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("operationId must not be null");
    }

    @Test
    void refusesAMissingBeneficiary() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      null,
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be null");
    }

    @Test
    void refusesAMissingAgreement() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      null,
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must not be null");
    }

    @Test
    void refusesAnEmptySnapshot() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      List.of(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installments must have at least one element");
    }

    @Test
    void refusesAMissingSnapshot() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      null,
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installments must have at least one element");
    }

    @Test
    void refusesABlankCreditor() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "  ",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("assigneeCreditor must not be blank");
    }

    @Test
    void refusesAMissingAccount() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      null,
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("receivingAccount must not be null");
    }

    @Test
    void refusesAMissingTotal() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      null,
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("totalAmount must not be null");
    }

    @Test
    void refusesAMissingStatus() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      null,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("status must not be null");
    }

    @Test
    void refusesARejectedOperationWithoutAReason() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.REJECTED,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("rejectionReason must not be null when rejected");
    }

    @Test
    void refusesAReasonOutsideTheRejectedStatus() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      new RejectionReason.MarginInsufficient(),
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("rejectionReason must be null unless rejected");
    }

    @Test
    void refusesACompletedOperationWithoutTheInstantOfTheAccountUpdate() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.COMPLETED,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("accountUpdatedAt must not be null when completed");
    }

    @Test
    void refusesAnAccountUpdateOutsideTheCompletedStatus() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      UPDATED_AT,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("accountUpdatedAt must be null unless completed");
    }

    @Test
    void refusesANegativeCountOfRetries() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      -1,
                      null,
                      CREATED_AT,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("retryCount must not be negative");
    }

    @Test
    void refusesAMissingCreationInstant() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      null,
                      UPDATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("createdAt must not be null");
    }

    @Test
    void refusesAMissingUpdateInstant() {
      assertThatThrownBy(
              () ->
                  new OperationDetail(
                      OPERATION_ID,
                      "PRT-20261127-0001",
                      new BeneficiaryId("BEN-001"),
                      new AgreementId("AGR-001"),
                      installments(),
                      "CREDOR-CESSIONARIO",
                      new ReceivingAccount("001", "1234", "56789-0"),
                      new Money(new BigDecimal("750.00")),
                      OperationStatus.PENDING,
                      null,
                      0,
                      null,
                      CREATED_AT,
                      null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("updatedAt must not be null");
    }
  }

  private static OperationSummary summary() {
    return summary(OPERATION_ID, "PRT-20261127-0001", new BeneficiaryId("BEN-001"));
  }

  private static OperationSummary summary(
      PortabilityOperation.OperationId id, String protocol, BeneficiaryId beneficiaryId) {
    return new OperationSummary(
        id,
        protocol,
        beneficiaryId,
        OperationStatus.PENDING,
        new Money(new BigDecimal("750.00")),
        CREATED_AT,
        UPDATED_AT);
  }

  private static OperationDetail detail() {
    return new OperationDetail(
        OPERATION_ID,
        "PRT-20261127-0001",
        new BeneficiaryId("BEN-001"),
        new AgreementId("AGR-001"),
        installments(),
        "CREDOR-CESSIONARIO",
        new ReceivingAccount("001", "1234", "56789-0"),
        new Money(new BigDecimal("750.00")),
        OperationStatus.PENDING,
        null,
        0,
        null,
        CREATED_AT,
        UPDATED_AT);
  }

  private static List<InstallmentSnapshot> installments() {
    return List.of(
        new InstallmentSnapshot(
            new InstallmentId("PAR-001"),
            new AgreementId("AGR-001"),
            new Money(new BigDecimal("450.00")),
            LocalDate.parse("2026-12-05")),
        new InstallmentSnapshot(
            new InstallmentId("PAR-002"),
            new AgreementId("AGR-001"),
            new Money(new BigDecimal("300.00")),
            LocalDate.parse("2026-12-20")));
  }
}
