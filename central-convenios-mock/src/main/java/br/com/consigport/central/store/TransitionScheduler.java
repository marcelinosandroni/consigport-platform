package br.com.consigport.central.store;

import br.com.consigport.central.domain.Operation;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Schedules the asynchronous confirmation of a portability operation.
 *
 * <p>After {@code mock.confirmation-delay-ms} the operation leaves {@code PENDENTE} and
 * becomes {@code CONFIRMADA} or {@code REJEITADA}. The executor is a single daemon
 * thread, so the mock stays lightweight and does not block shutdown.
 */
@Component
public class TransitionScheduler {

  private final InMemoryCentralStore store;
  private final long delayMs;
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "mock-transition");
            thread.setDaemon(true);
            return thread;
          });

  public TransitionScheduler(
      InMemoryCentralStore store,
      @Value("${mock.confirmation-delay-ms:2000}") long delayMs) {
    this.store = store;
    this.delayMs = delayMs;
  }

  /** Schedules the terminal transition of an accepted operation. */
  public void agendar(Operation operacao) {
    executor.schedule(() -> store.concluir(operacao), delayMs, TimeUnit.MILLISECONDS);
  }

  @PreDestroy
  void encerrar() {
    executor.shutdownNow();
  }
}
