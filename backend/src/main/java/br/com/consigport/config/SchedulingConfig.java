package br.com.consigport.config;

import br.com.consigport.application.portability.PollOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * The worker that follows the operations until the Central answers.
 *
 * <p>The worker wakes up every 2 s and polls a batch of 20 operations, as
 * {@code ARCHITECTURE.md} §11 defines. One thread runs the batch: the worker is a single
 * instance by trade-off, and the optimistic lock of the storage guards the operations
 * that another instance may be moving at the same time.
 *
 * <p>The polling can be switched off with {@code central.poll.enabled}, which the
 * integration tests use: the worker would race with the assertions of the tests.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulingConfig.class);

  private final PollOperation pollOperation;
  private final boolean enabled;

  /**
   * Builds the configuration.
   *
   * @param pollOperation the use case that polls the operations
   * @param enabled whether the worker runs
   */
  public SchedulingConfig(
      PollOperation pollOperation, @Value("${central.poll.enabled:true}") boolean enabled) {
    this.pollOperation = pollOperation;
    this.enabled = enabled;
  }

  /**
   * The scheduler of the worker: one thread, because the worker is a single instance.
   *
   * @return the scheduler
   */
  @Bean
  public TaskScheduler pollTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("consigport-poll-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(10);
    return scheduler;
  }

  /**
   * Polls the batch of operations that is due.
   */
  @Scheduled(fixedDelayString = "${central.poll.fixed-delay:2s}")
  public void pollPortabilityOperations() {
    if (!enabled) {
      return;
    }
    try {
      PollOperation.PollResult result = pollOperation.pollBatch();
      if (result.polled() > 0) {
        LOG.info("polled {} portability operations", result.polled());
      }
    } catch (RuntimeException exception) {
      LOG.warn("the polling batch failed: {}", exception.getMessage());
    }
  }
}
