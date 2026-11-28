package br.com.consigport.domain.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the read models of the agenda of discounts.
 *
 * <p>The agenda lives in the Central and is translated by the anti-corruption layer: the
 * read model refuses an incomplete installment, and only the {@code OPEN} installments are
 * eligible for a portability operation (BR-03).
 */
class AgendaReadModelsTest {

  @Nested
  class InstallmentTest {

    @Test
    void carriesTheInstallmentOfTheAgreement() {
      Installment installment =
          new Installment(
              new InstallmentId("PAR-001"),
              new AgreementId("AGR-001"),
              new Money(new BigDecimal("450.00")),
              LocalDate.parse("2026-12-05"),
              InstallmentStatus.OPEN);

      assertThat(installment.installmentId()).isEqualTo(new InstallmentId("PAR-001"));
      assertThat(installment.agreementId()).isEqualTo(new AgreementId("AGR-001"));
      assertThat(installment.amount()).isEqualTo(new Money(new BigDecimal("450.00")));
      assertThat(installment.discountDate()).isEqualTo(LocalDate.parse("2026-12-05"));
      assertThat(installment.status()).isEqualTo(InstallmentStatus.OPEN);
    }

    @Test
    void refusesAMissingInstallment() {
      assertThatThrownBy(
              () ->
                  new Installment(
                      null,
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("450.00")),
                      LocalDate.parse("2026-12-05"),
                      InstallmentStatus.OPEN))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentId must not be null");
    }

    @Test
    void refusesAMissingAgreement() {
      assertThatThrownBy(
              () ->
                  new Installment(
                      new InstallmentId("PAR-001"),
                      null,
                      new Money(new BigDecimal("450.00")),
                      LocalDate.parse("2026-12-05"),
                      InstallmentStatus.OPEN))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must not be null");
    }

    @Test
    void refusesAMissingAmount() {
      assertThatThrownBy(
              () ->
                  new Installment(
                      new InstallmentId("PAR-001"),
                      new AgreementId("AGR-001"),
                      null,
                      LocalDate.parse("2026-12-05"),
                      InstallmentStatus.OPEN))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("amount must not be null");
    }

    @Test
    void refusesAMissingDate() {
      assertThatThrownBy(
              () ->
                  new Installment(
                      new InstallmentId("PAR-001"),
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("450.00")),
                      null,
                      InstallmentStatus.OPEN))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("discountDate must not be null");
    }

    @Test
    void refusesAMissingStatus() {
      assertThatThrownBy(
              () ->
                  new Installment(
                      new InstallmentId("PAR-001"),
                      new AgreementId("AGR-001"),
                      new Money(new BigDecimal("450.00")),
                      LocalDate.parse("2026-12-05"),
                      null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("status must not be null");
    }
  }

  @Nested
  class BeneficiaryAgendaTest {

    @Test
    void listsOnlyTheOpenInstallmentsAsEligible() {
      BeneficiaryAgenda agenda =
          new BeneficiaryAgenda(
              new BeneficiaryId("BEN-001"),
              List.of(
                  installment("PAR-001", InstallmentStatus.OPEN),
                  installment("PAR-002", InstallmentStatus.CLOSED)));

      assertThat(agenda.installments()).hasSize(2);
      assertThat(agenda.openInstallments())
          .extracting(Installment::installmentId)
          .containsExactly(new InstallmentId("PAR-001"));
    }

    @Test
    void acceptsABeneficiaryWithoutInstallments() {
      BeneficiaryAgenda agenda = new BeneficiaryAgenda(new BeneficiaryId("BEN-001"), null);

      assertThat(agenda.installments()).isEmpty();
      assertThat(agenda.openInstallments()).isEmpty();
    }

    @Test
    void freezesTheInstallments() {
      BeneficiaryAgenda agenda =
          new BeneficiaryAgenda(
              new BeneficiaryId("BEN-001"), List.of(installment("PAR-001", InstallmentStatus.OPEN)));

      assertThatThrownBy(() -> agenda.installments().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void refusesAMissingBeneficiary() {
      assertThatThrownBy(() -> new BeneficiaryAgenda(null, List.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be null");
    }
  }

  private static Installment installment(String id, InstallmentStatus status) {
    return new Installment(
        new InstallmentId(id),
        new AgreementId("AGR-001"),
        new Money(new BigDecimal("450.00")),
        LocalDate.parse("2026-12-05"),
        status);
  }
}
