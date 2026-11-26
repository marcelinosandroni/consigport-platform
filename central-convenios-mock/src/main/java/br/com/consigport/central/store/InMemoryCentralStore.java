package br.com.consigport.central.store;

import br.com.consigport.central.domain.Authorization;
import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Operation;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
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

  private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final ConcurrentMap<String, List<Installment>> agenda = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Authorization> autorizacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, String> autorizacaoPorChave = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Operation> operacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Integer> leiturasDeStatus = new ConcurrentHashMap<>();
  private final AtomicLong sequenciaAutorizacao = new AtomicLong();

  public InMemoryCentralStore() {
    reset();
  }

  /** Restores the fixtures and clears every operation and counter. */
  public synchronized void reset() {
    agenda.clear();
    autorizacoes.clear();
    autorizacaoPorChave.clear();
    operacoes.clear();
    leiturasDeStatus.clear();
    sequenciaAutorizacao.set(0);
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
    autorizacaoPorChave.put(chave(authorization.beneficiarioId(), authorization.convenioId()),
        authorization.autorizacaoId());
  }

  /** Tells whether the beneficiary has at least one opt-in registered. */
  public boolean possuiAutorizacao(String beneficiarioId) {
    return autorizacoes.values().stream()
        .anyMatch(autorizacao -> autorizacao.beneficiarioId().equals(beneficiarioId));
  }

  /**
   * Registers the opt-in of a beneficiary for an agreement, or returns the existing one.
   *
   * <p>BR-02: the pair {@code (beneficiarioId, convenioId)} is unique, so repeating the
   * request yields the same authorization instead of creating a duplicate.
   */
  public synchronized RegistroAutorizacao autorizar(
      String beneficiarioId, String convenioId, Instant criadaEm) {
    String existenteId = autorizacaoPorChave.get(chave(beneficiarioId, convenioId));
    if (existenteId != null) {
      return new RegistroAutorizacao(autorizacoes.get(existenteId), false);
    }

    String dia = DIA.format(criadaEm.atZone(ZoneOffset.UTC));
    String autorizacaoId =
        "AUT-%s-%04d".formatted(dia, sequenciaAutorizacao.incrementAndGet());
    Authorization nova =
        new Authorization(autorizacaoId, beneficiarioId, convenioId, criadaEm);
    autorizacoes.put(autorizacaoId, nova);
    autorizacaoPorChave.put(chave(beneficiarioId, convenioId), autorizacaoId);
    return new RegistroAutorizacao(nova, true);
  }

  private static String chave(String beneficiarioId, String convenioId) {
    return beneficiarioId + '\u0000' + convenioId;
  }

  /** Outcome of an opt-in request: the authorization and whether it was just created. */
  public record RegistroAutorizacao(Authorization autorizacao, boolean criada) {}

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
