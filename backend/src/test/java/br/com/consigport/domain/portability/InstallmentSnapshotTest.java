package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the frozen installment picture.
 *
 * <p>The snapshot is immutable and never carries a missing field.
 */
class InstallmentSnapshotTest {

  @Test
  void keepsTheFrozenData() {
    InstallmentSnapshot snapshot =
        new InstallmentSnapshot(
            new InstallmentId("PAR-001"),
            new AgreementId("AGR-001"),
            new Money(new BigDecimal("450.00")),
            LocalDate.parse("2026-12-05"));

    assertThat(snapshot.installmentId()).isEqualTo(new InstallmentId("PAR-001"));
    assertThat(snapshot.agreementId()).isEqualTo(new AgreementId("AGR-001"));
    assertThat(snapshot.amount().amount()).isEqualTo(new BigDecimal("450.00"));
    assertThat(snapshot.discountDate()).isEqualTo(LocalDate.parse("2026-12-05"));
  }

  @Test
  void comparesByValue() {
    InstallmentSnapshot snapshot =
        new InstallmentSnapshot(
            new InstallmentId("PAR-001"),
            new AgreementId("AGR-001"),
            new Money(new BigDecimal("450.00")),
            LocalDate.parse("2026-12-05"));
    InstallmentSnapshot same =
        new InstallmentSnapshot(
            new InstallmentId("PAR-001"),
            new AgreementId("AGR-001"),
            new Money(new BigDecimal("450.00")),
            LocalDate.parse("2026-12-05"));

    assertThat(snapshot).isEqualTo(same);
  }

  @Test
  void refusesAMissingInstallment() {
    assertThatThrownBy(
            () ->
                new InstallmentSnapshot(
                    null,
                    new AgreementId("AGR-001"),
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("installmentId must not be null");
  }

  @Test
  void refusesAMissingAgreement() {
    assertThatThrownBy(
            () ->
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"),
                    null,
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("agreementId must not be null");
  }

  @Test
  void refusesAMissingAmount() {
    assertThatThrownBy(
            () ->
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"), new AgreementId("AGR-001"), null, LocalDate.parse("2026-12-05")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("amount must not be null");
  }

  @Test
  void refusesAMissingDiscountDate() {
    assertThatThrownBy(
            () ->
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"),
                    new AgreementId("AGR-001"),
                    new Money(new BigDecimal("450.00")),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("discountDate must not be null");
  }
}
