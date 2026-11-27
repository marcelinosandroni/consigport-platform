package br.com.consigport.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.application.agenda.QueryAgenda;
import br.com.consigport.config.MessageSourceConfig;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.agenda.Installment;
import br.com.consigport.domain.agenda.InstallmentStatus;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.support.FixedClockConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The public edge of the agenda of discounts of a beneficiary.
 *
 * <p>The slice loads the resource and the advice of the errors: the use case is replaced
 * by a double, so the test proves the wire and the translation of the refusals of the
 * Central em `403` e `404`.
 */
@WebMvcTest(AgendaResource.class)
@Import({FixedClockConfig.class, MessageSourceConfig.class})
class AgendaResourceTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private QueryAgenda queryAgenda;

  @Test
  void answersTheAgendaWithEveryInstallment() throws Exception {
    when(queryAgenda.execute(any(BeneficiaryId.class))).thenReturn(agenda());

    mvc.perform(get("/api/v1/beneficiaries/BEN-001/agenda"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.beneficiaryId").value("BEN-001"))
        .andExpect(jsonPath("$.installments.length()").value(2))
        .andExpect(jsonPath("$.installments[0].installmentId").value("PAR-001"))
        .andExpect(jsonPath("$.installments[0].agreementId").value("AGR-001"))
        .andExpect(jsonPath("$.installments[0].amount").value(450.00))
        .andExpect(jsonPath("$.installments[0].discountDate").value("2026-12-05"))
        .andExpect(jsonPath("$.installments[0].status").value("OPEN"))
        .andExpect(jsonPath("$.installments[1].status").value("CLOSED"));
  }

  @Test
  void answersForbiddenWhenTheBeneficiaryHasNoOptIn() throws Exception {
    when(queryAgenda.execute(any(BeneficiaryId.class)))
        .thenThrow(new DomainError.AuthorizationRequired());

    mvc.perform(get("/api/v1/beneficiaries/BEN-001/agenda"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTHORIZATION_REQUIRED"))
        .andExpect(jsonPath("$.path").value("/api/v1/beneficiaries/BEN-001/agenda"));
  }

  @Test
  void answersNotFoundWhenTheCentralDoesNotKnowTheBeneficiary() throws Exception {
    when(queryAgenda.execute(any(BeneficiaryId.class)))
        .thenThrow(new DomainError.BeneficiaryNotFound(new BeneficiaryId("BEN-001")));

    mvc.perform(get("/api/v1/beneficiaries/BEN-404/agenda"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("BENEFICIARY_NOT_FOUND"));
  }

  private static BeneficiaryAgenda agenda() {
    return new BeneficiaryAgenda(
        new BeneficiaryId("BEN-001"),
        List.of(
            new Installment(
                new InstallmentId("PAR-001"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("450.00")),
                LocalDate.parse("2026-12-05"),
                InstallmentStatus.OPEN),
            new Installment(
                new InstallmentId("PAR-003"),
                new AgreementId("AGR-001"),
                new Money(new BigDecimal("100.00")),
                LocalDate.parse("2027-02-05"),
                InstallmentStatus.CLOSED)));
  }
}
