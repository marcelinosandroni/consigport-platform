package br.com.consigport.central.store;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Keeps the payload hash of each {@code Idempotency-Key} and the protocol it produced.
 *
 * <p>BR-07: the same key with the same payload returns the existing operation; the same
 * key with a different payload is a conflict. The registry is cleared by the store reset.
 */
@Component
public class IdempotencyRegistry {

  /** Payload hash and protocol associated with a key. */
  public record Entry(String payloadHash, String protocolo) {}

  private final ConcurrentMap<String, Entry> entradas = new ConcurrentHashMap<>();

  /** Returns the entry of a key, when it was already used. */
  public Optional<Entry> buscar(String chave) {
    return Optional.ofNullable(entradas.get(chave));
  }

  /** Records the key and the operation it produced. */
  public void registrar(String chave, Entry entry) {
    entradas.put(chave, entry);
  }

  /** Clears every key; called by the administrative reset. */
  public void clear() {
    entradas.clear();
  }
}
