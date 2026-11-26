package br.com.consigport.central.api;

import br.com.consigport.central.api.dto.PortabilityAcceptedResponse;
import br.com.consigport.central.api.dto.PortabilityRequest;
import br.com.consigport.central.api.dto.StatusResponse;
import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Installment.InstallmentStatus;
import br.com.consigport.central.domain.Operation;
import br.com.consigport.central.domain.OperationStatus;
import br.com.consigport.central.store.InMemoryCentralStore;
import br.com.consigport.central.store.TransitionScheduler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Submission of portability operations.
 *
 * <p>Mirrors {@code POST /operacoes-portabilidade}: the operation is accepted with a
 * protocol and starts as {@code PENDENTE}; the confirmation is asynchronous (task 1.5).
 * Installments that do not belong to the beneficiary, or that are not {@code ABERTA},
 * produce {@code 422 PARCELA_INVALIDA} (BR-04).
 */
@RestController
@RequestMapping("/operacoes-portabilidade")
@Tag(name = "Portabilidade")
public class PortabilityController {

  private final InMemoryCentralStore store;
  private final TransitionScheduler scheduler;

  public PortabilityController(InMemoryCentralStore store, TransitionScheduler scheduler) {
    this.store = store;
    this.scheduler = scheduler;
  }

  @PostMapping
  @io.swagger.v3.oas.annotations.Operation(summary = "Registra uma operação de portabilidade")
  public ResponseEntity<PortabilityAcceptedResponse> registrar(
      @Valid @RequestBody PortabilityRequest request) {
    validarParcelas(request.beneficiarioId(), request.parcelas());

    Operation operacao =
        store.criarOperacao(request.beneficiarioId(), request.parcelas(), Instant.now());
    scheduler.agendar(operacao);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new PortabilityAcceptedResponse(operacao.protocolo()));
  }

  @GetMapping("/{protocolo}")
  @io.swagger.v3.oas.annotations.Operation(summary = "Consulta o status da operação")
  public ResponseEntity<StatusResponse> consultarStatus(@PathVariable String protocolo) {
    Operation operacao =
        store
            .operacao(protocolo)
            .orElseThrow(
                () ->
                    new CentralException(
                        HttpStatus.NOT_FOUND,
                        "OPERACAO_NAO_ENCONTRADA",
                        "Operação não encontrada para o protocolo informado."));

    if (operacao.status() == OperationStatus.PENDENTE) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT)
          .header(HttpHeaders.RETRY_AFTER, "2")
          .build();
    }
    return ResponseEntity.ok(new StatusResponse(operacao.status(), operacao.motivoRejeicao()));
  }

  private void validarParcelas(String beneficiarioId, List<String> parcelas) {
    List<Installment> agenda = store.agendaDe(beneficiarioId);
    for (String parcelaId : parcelas) {
      Installment parcela =
          agenda.stream()
              .filter(candidata -> candidata.parcelaId().equals(parcelaId))
              .findFirst()
              .orElseThrow(
                  () ->
                      new CentralException(
                          HttpStatus.UNPROCESSABLE_ENTITY,
                          "PARCELA_INVALIDA",
                          "A parcela %s não existe.".formatted(parcelaId)));
      if (parcela.status() != InstallmentStatus.ABERTA) {
        throw new CentralException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "PARCELA_INVALIDA",
            "A parcela %s não está aberta.".formatted(parcelaId));
      }
    }
  }
}
