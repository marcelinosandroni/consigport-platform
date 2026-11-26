package br.com.consigport.central.store;

import br.com.consigport.central.domain.Authorization;
import br.com.consigport.central.domain.Installment;
import br.com.consigport.central.domain.Operation;
import br.com.consigport.central.domain.OperationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private static final BigDecimal LIMITE_MARGEM = new BigDecimal("5000.00");
  private static final String MOTIVO_MARGEM = "MARGEM_CONSIGNADA_INSUFICIENTE";

  private final ConcurrentMap<String, List<Installment>> agenda = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Authorization> autorizacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, String> autorizacaoPorChave = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Operation> operacoes = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Integer> leiturasDeStatus = new ConcurrentHashMap<>();
  private final AtomicLong sequenciaAutorizacao = new AtomicLong();
  private final AtomicLong sequenciaOperacao = new AtomicLong();
  private final IdempotencyRegistry idempotencia;

  public InMemoryCentralStore(IdempotencyRegistry idempotencia) {
    this.idempotencia = idempotencia;
    reset();
  }

  /** Restores the fixtures and clears every operation, counter and idempotency key. */
  public synchronized void reset() {
    agenda.clear();
    idempotencia.clear();
    autorizacoes.clear();
    autorizacaoPorChave.clear();
    operacoes.clear();
    leiturasDeStatus.clear();
    sequenciaAutorizacao.set(0);
    sequenciaOperacao.set(0);
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
   * Accepts a portability operation with a fresh protocol and the {@code PENDENTE} status.
   *
   * <p>The protocol follows the contract pattern {@code PRT-yyyyMMdd-NNNN}; the sequence
   * is zeroed by {@link #reset()}.
   */
  public synchronized Operation criarOperacao(
      String beneficiarioId, List<String> parcelas, Instant criadaEm) {
    String dia = DIA.format(criadaEm.atZone(ZoneOffset.UTC));
    String protocolo = "PRT-%s-%04d".formatted(dia, sequenciaOperacao.incrementAndGet());
    Operation operacao =
        new Operation(
            protocolo,
            beneficiarioId,
            parcelas,
            OperationStatus.PENDENTE,
            null,
            criadaEm);
    operacoes.put(protocolo, operacao);
    return operacao;
  }

  /** Returns an operation by protocol, when it exists. */
  public Optional<Operation> operacao(String protocolo) {
    return Optional.ofNullable(operacoes.get(protocolo));
  }

  /**
   * Applies the terminal status to a previously accepted operation.
   *
   * <p>The instance is compared by identity so a task scheduled before a {@link #reset()}
   * cannot mutate a fresh operation that happens to reuse the same protocol.
   * Deterministic rejection: beneficiary ending in {@code -REJ} or installments summing
   * more than {@code 5000.00} (BR-14).
   */
  public synchronized void concluir(Operation agendada) {
    Operation atual = operacoes.get(agendada.protocolo());
    if (atual != agendada || atual.status() != OperationStatus.PENDENTE) {
      return;
    }

    boolean rejeitar =
        atual.beneficiarioId().endsWith("-REJ") || somaDasParcelas(atual).compareTo(LIMITE_MARGEM) > 0;
    Operation concluida =
        rejeitar
            ? atual.withStatus(OperationStatus.REJEITADA, MOTIVO_MARGEM)
            : atual.withStatus(OperationStatus.CONFIRMADA, null);
    operacoes.put(concluida.protocolo(), concluida);
  }

  private BigDecimal somaDasParcelas(Operation operacao) {
    List<Installment> agendaDoBeneficiario = agendaDe(operacao.beneficiarioId());
    return operacao.parcelas().stream()
        .map(
            parcelaId ->
                agendaDoBeneficiario.stream()
                    .filter(parcela -> parcela.parcelaId().equals(parcelaId))
                    .findFirst()
                    .map(Installment::valor)
                    .orElse(BigDecimal.ZERO))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
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
