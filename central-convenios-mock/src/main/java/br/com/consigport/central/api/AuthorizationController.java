package br.com.consigport.central.api;

import br.com.consigport.central.api.dto.AuthorizationRequest;
import br.com.consigport.central.api.dto.AuthorizationResponse;
import br.com.consigport.central.store.InMemoryCentralStore;
import br.com.consigport.central.store.InMemoryCentralStore.RegistroAutorizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Beneficiary opt-in: the consent that unlocks the agenda.
 *
 * <p>Mirrors {@code POST /autorizacoes}. Repeating the same pair returns {@code 200}
 * with the existing identifier instead of duplicating the authorization (BR-02).
 */
@RestController
@RequestMapping("/autorizacoes")
@Tag(name = "Autorizações")
public class AuthorizationController {

  private final InMemoryCentralStore store;

  public AuthorizationController(InMemoryCentralStore store) {
    this.store = store;
  }

  @PostMapping
  @Operation(summary = "Registra a autorização (opt-in)")
  public ResponseEntity<AuthorizationResponse> registrar(
      @Valid @RequestBody AuthorizationRequest request) {
    RegistroAutorizacao registro =
        store.autorizar(request.beneficiarioId(), request.convenioId(), Instant.now());
    AuthorizationResponse body =
        new AuthorizationResponse(registro.autorizacao().autorizacaoId());

    return registro.criada()
        ? ResponseEntity.status(HttpStatus.CREATED).body(body)
        : ResponseEntity.ok(body);
  }
}
