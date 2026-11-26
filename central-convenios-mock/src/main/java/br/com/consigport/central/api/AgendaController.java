package br.com.consigport.central.api;

import br.com.consigport.central.api.dto.InstallmentResponse;
import br.com.consigport.central.domain.Installment.InstallmentStatus;
import br.com.consigport.central.store.InMemoryCentralStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Future installments of an authorized beneficiary.
 *
 * <p>Mirrors {@code GET /beneficiarios/{beneficiarioId}/agenda}: the contract returns a
 * bare array of {@code Parcela}, so no envelope is added here.
 */
@RestController
@RequestMapping("/beneficiarios")
@Tag(name = "Agenda")
public class AgendaController {

  private final InMemoryCentralStore store;

  public AgendaController(InMemoryCentralStore store) {
    this.store = store;
  }

  @GetMapping("/{beneficiarioId}/agenda")
  @Operation(summary = "Consulta a agenda de descontos futuros")
  public List<InstallmentResponse> consultar(@PathVariable String beneficiarioId) {
    if (!store.possuiBeneficiario(beneficiarioId)) {
      throw new CentralException(
          HttpStatus.NOT_FOUND, "BENEFICIARIO_NAO_ENCONTRADO", "Beneficiário não encontrado.");
    }
    if (!store.possuiAutorizacao(beneficiarioId)) {
      throw new CentralException(
          HttpStatus.FORBIDDEN,
          "SEM_AUTORIZACAO",
          "O beneficiário não possui autorização para este convênio.");
    }
    return store.agendaDe(beneficiarioId).stream()
        .filter(installment -> installment.status() == InstallmentStatus.ABERTA)
        .map(InstallmentResponse::from)
        .toList();
  }
}
