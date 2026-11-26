package br.com.consigport.central.api;

import br.com.consigport.central.store.InMemoryCentralStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative endpoints of the mock: reset the state and inspect it.
 *
 * <p>They are test-only, as stated in {@code contracts/central-api.yaml}, and exist so
 * every scenario starts from the same deterministic fixtures.
 */
@RestController
@RequestMapping("/_admin")
@Tag(name = "Administração")
public class AdminController {

  private final InMemoryCentralStore store;

  public AdminController(InMemoryCentralStore store) {
    this.store = store;
  }

  @PostMapping("/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Restaura as fixtures e zera operações")
  public void resetar() {
    store.reset();
  }

  @GetMapping("/state")
  @Operation(summary = "Inspeciona o estado em memória")
  public Map<String, Object> estado() {
    return store.snapshot();
  }
}
