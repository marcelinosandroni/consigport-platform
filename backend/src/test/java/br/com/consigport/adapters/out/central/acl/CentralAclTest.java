package br.com.consigport.adapters.out.central.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.adapters.out.central.dto.AgendaResponse;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Translation between the contract of the Central and the domain.
 *
 * <p>The Central speaks Portuguese; the domain speaks English. The mapping is checked in
 * both directions and the refusals of the Central become domain errors.
 */
class CentralAclTest {

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");

  private final CentralAcl acl = new CentralAcl();

  @Test
  void translatesTheAgendaAnsweredByTheCentral() {
    BeneficiaryAgenda agenda =
        acl.toAgenda(
            BENEFICIARY,
            List.of(
                parcela("PAR-001", "AGR-001", "450.00", "2026-12-05", "ABERTA"),
                parcela("PAR-002", "AGR-001", "300.00", "2027-01-05", "FECHADA")));

    assertThat(agenda.beneficiaryId()).isEqualTo(BENEFICIARY);
    assertThat(agenda.installments()).hasSize(2);
    assertThat(agenda.installments().get(0).installmentId())
        .isEqualTo(new InstallmentId("PAR-001"));
    assertThat(agenda.installments().get(0).agreementId()).isEqualTo(new AgreementId("AGR-001"));
    assertThat(agenda.installments().get(0).amount().amount())
        .isEqualTo(new BigDecimal("450.00"));
    assertThat(agenda.installments().get(0).discountDate())
        .isEqualTo(LocalDate.parse("2026-12-05"));
    assertThat(agenda.installments().get(0).status()).isEqualTo(InstallmentStatus.OPEN);
    assertThat(agenda.installments().get(1).status()).isEqualTo(InstallmentStatus.CLOSED);
  }

  @Test
  void keepsOnlyTheOpenInstallmentsAsEligible() {
    BeneficiaryAgenda agenda =
        acl.toAgenda(
            BENEFICIARY,
            List.of(
                parcela("PAR-001", "AGR-001", "450.00", "2026-12-05", "ABERTA"),
                parcela("PAR-002", "AGR-001", "300.00", "2027-01-05", "FECHADA")));

    assertThat(agenda.openInstallments())
        .extracting(installment -> installment.installmentId().value())
        .containsExactly("PAR-001");
  }

  @Nested
  class StatusMapping {

    @Test
    void readsTheOpenStatusOfTheCentral() {
      assertThat(acl.toDomainStatus("ABERTA")).isEqualTo(InstallmentStatus.OPEN);
    }

    @Test
    void readsTheClosedStatusOfTheCentral() {
      assertThat(acl.toDomainStatus("FECHADA")).isEqualTo(InstallmentStatus.CLOSED);
    }

    @Test
    void writesTheOpenStatusOfTheCentral() {
      assertThat(acl.toCentralStatus(InstallmentStatus.OPEN)).isEqualTo("ABERTA");
    }

    @Test
    void writesTheClosedStatusOfTheCentral() {
      assertThat(acl.toCentralStatus(InstallmentStatus.CLOSED)).isEqualTo("FECHADA");
    }

    @Test
    void roundTripsEveryStatus() {
      for (InstallmentStatus status : InstallmentStatus.values()) {
        assertThat(acl.toDomainStatus(acl.toCentralStatus(status))).isEqualTo(status);
      }
    }

    @Test
    void refusesAnUnknownStatus() {
      assertThatThrownBy(() -> acl.toDomainStatus("PARCADA"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown installment status: PARCADA");
    }

    @Test
    void refusesABlankStatus() {
      assertThatThrownBy(() -> acl.toDomainStatus(" "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installment status must not be blank");
    }
  }

  @Nested
  class ErrorMapping {

    @Test
    void translatesTheRefusalWithoutAuthorization() {
      DomainError error = acl.toDomainError(403, BENEFICIARY);

      assertThat(error).isInstanceOf(DomainError.AuthorizationRequired.class);
      assertThat(error.code()).isEqualTo("AUTHORIZATION_REQUIRED");
    }

    @Test
    void translatesTheUnknownBeneficiary() {
      DomainError error = acl.toDomainError(404, BENEFICIARY);

      assertThat(error).isInstanceOf(DomainError.BeneficiaryNotFound.class);
      assertThat(error.code()).isEqualTo("BENEFICIARY_NOT_FOUND");
      assertThat(((DomainError.BeneficiaryNotFound) error).beneficiaryId())
          .isEqualTo(BENEFICIARY);
    }

    @Test
    void refusesAnUnexpectedStatus() {
      assertThatThrownBy(() -> acl.toDomainError(500, BENEFICIARY))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unexpected Central status: 500");
    }
  }

  private static AgendaResponse parcela(
      String installmentId, String agreementId, String amount, String discountDate, String status) {
    return new AgendaResponse(
        installmentId,
        agreementId,
        new BigDecimal(amount),
        LocalDate.parse(discountDate),
        status);
  }
}
