package br.com.consigport.central.chaos;

import br.com.consigport.central.api.CentralException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Deterministic chaos of the mock, as defined in {@code SDD/ARCHITECTURE.md}.
 *
 * <p>There is no randomness: the {@code -TIMEOUT} suffix sleeps on the first two status
 * reads of each operation and the headers {@code X-Mock-Delay-Ms} and
 * {@code X-Mock-Force-Error} affect only the request that carries them.
 */
@Component
public class ChaosPolicy {

  private final long timeoutDelayMs;
  private final ConcurrentMap<String, AtomicInteger> leituras = new ConcurrentHashMap<>();

  public ChaosPolicy(@Value("${mock.timeout-delay-ms:6000}") long timeoutDelayMs) {
    this.timeoutDelayMs = timeoutDelayMs;
  }

  /**
   * Sleeps for the {@code X-Mock-Delay-Ms} header, when present and valid.
   *
   * <p>The delay belongs to a single response; it is never persisted.
   */
  public void aplicarAtraso(String headerDelayMs) {
    long delay = parse(headerDelayMs);
    if (delay > 0) {
      dormir(delay);
    }
  }

  /**
   * Rejects the request with the status of the {@code X-Mock-Force-Error} header.
   *
   * <p>Only {@code 500}, {@code 502} and {@code 503} are honoured, matching the enum of
   * the contract; any other value is ignored.
   */
  public void aplicarErroForcado(String headerForceError) {
    long status = parse(headerForceError);
    if (status != 500 && status != 502 && status != 503) {
      return;
    }
    throw new CentralException(
        HttpStatus.valueOf((int) status), "ERRO_FORCADO", "Erro forçado pelo mock.");
  }

  /**
   * Sleeps on the first two status reads of an operation whose beneficiary ends in
   * {@code -TIMEOUT}; the third read and the following ones answer normally.
   */
  public void aplicarTimeoutDeterministico(String protocolo, String beneficiarioId) {
    if (beneficiarioId == null || !beneficiarioId.endsWith("-TIMEOUT")) {
      return;
    }
    int leitura =
        leituras.computeIfAbsent(protocolo, chave -> new AtomicInteger()).incrementAndGet();
    if (leitura <= 2) {
      dormir(timeoutDelayMs);
    }
  }

  /** Clears the per-operation read counters; called by the administrative reset. */
  public void reset() {
    leituras.clear();
  }

  /** Returns the per-operation read counters, sorted for the deterministic snapshot. */
  public SortedMap<String, Integer> contadores() {
    SortedMap<String, Integer> copia = new TreeMap<>();
    leituras.forEach((protocolo, contador) -> copia.put(protocolo, contador.get()));
    return copia;
  }

  private static long parse(String value) {
    if (value == null || value.isBlank()) {
      return -1;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException exception) {
      return -1;
    }
  }

  private static void dormir(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
