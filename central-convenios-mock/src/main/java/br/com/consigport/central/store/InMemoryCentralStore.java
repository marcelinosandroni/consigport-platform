package br.com.consigport.central.store;

import br.com.consigport.central.domain.Authorization;
import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Operation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Deterministic, restartable state of the simulated Central.
 *
 * <p>Everything lives in {@link ConcurrentHashMap}s: there is no database and no Flyway.
 * {@link #reset()} restores the fixtures from {@link SeedData} and zeroes operations and
 * counters, which is what the admin endpoints and the integration tests rely on.
 */
@Component
public class InMemoryCentralStore {

  private final ConcurrentMap<String, List<Installment>> agenda = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Authorization> autorizacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Operation> operacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Integer> leiturasDeStatus = new ConcurrentHashMap<>();

  public InMemoryCentralStore() {
    reset();
  }

  /** Restores the fixtures and clears every operation and counter. */
  public synchronized void reset() {
    agenda.clear();
    autorizacoes.clear();
    operacoes.clear();
    leiturasDeStatus.clear();
    SeedData.fixtures().forEach((beneficiarioId, parcelas) -> agenda.put(beneficiarioId, parcelas));
  }

  /** Returns the agenda of a beneficiary, or an empty list when it is unknown. */
  public List<Installment> agendaDe(String beneficiarioId) {
    return agenda.getOrDefault(beneficiarioId, List.of());
  }

  /** Tells whether the beneficiary has fixtures registered. */
  public boolean possuiBeneficiario(String beneficiarioId) {
    return agenda.containsKey(beneficiarioId);
  }

  /** Records an opt-in so the agenda of the beneficiary can be consulted. */
  public void registrarAutorizacao(Authorization authorization) {
    autorizacoes.put(authorization.autorizacaoId(), authorization);
  }

  /** Tells whether the beneficiary has at least one opt-in registered. */
  public boolean possuiAutorizacao(String beneficiarioId) {
    return autorizacoes.values().stream()
        .anyMatch(autorizacao -> autorizacao.beneficiarioId().equals(beneficiarioId));
  }

  /**
   * Returns the complete state for {@code GET /_admin/state}.
   *
   * <p>Keys are sorted so two resets produce byte-identical JSON, which the integration
   * test uses as its determinism proof.
   */
  public Map<String, Object> snapshot() {
    Map<String, Object> estado = new LinkedHashMap<>();
    estado.put("beneficiarios", new TreeMap<>(agenda));
    estado.put("autorizacoes", new TreeMap<>(autorizacoes));
    estado.put("operacoes", new TreeMap<>(operacoes));
    estado.put("contadores", Map.of("leiturasDeStatus", new TreeMap<>(leiturasDeStatus)));
    return estado;
  }
}
