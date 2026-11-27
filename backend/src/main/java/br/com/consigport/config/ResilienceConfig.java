package br.com.consigport.config;

import br.com.consigport.adapters.out.resilience.CircuitBreaker;
import br.com.consigport.application.portability.IntegrationBackoff;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience of the calls to the Central.
 *
 * <p>The values come from {@code ARCHITECTURE.md} §11: a circuit with a window of 10
 * calls that opens at 50% of failures, stays open for 10 s and closes after three calls
 * in half-open state; a backoff that doubles with every failure and never passes 60 s.
 *
 * <p>The class only wires the policies: the values come from the environment and the
 * behaviour is proven by the tests of each policy.
 */
@Configuration
public class ResilienceConfig {

  /**
   * The circuit that protects the calls to the Central.
   *
   * @param clock the clock that measures the open duration
   * @param windowSize how many outcomes the window remembers
   * @param failureThreshold the ratio of failures that opens the circuit
   * @param openDuration how long the circuit stays open
   * @param halfOpenCalls how many calls in half-open state close the circuit
   * @return the circuit of the calls to the Central
   */
  @Bean
  public CircuitBreaker centralCircuitBreaker(
      Clock clock,
      @Value("${central.circuit.window-size:10}") int windowSize,
      @Value("${central.circuit.failure-threshold:0.5}") double failureThreshold,
      @Value("${central.circuit.open-duration:10s}") Duration openDuration,
      @Value("${central.circuit.half-open-calls:3}") int halfOpenCalls) {
    return new CircuitBreaker(
        windowSize, failureThreshold, openDuration, halfOpenCalls, clock);
  }

  /**
   * The backoff of the operations whose polling failed.
   *
   * @param base the wait of the first failure
   * @param ceiling the longest wait between two attempts
   * @return the backoff of the polling
   */
  @Bean
  public IntegrationBackoff pollBackoff(
      @Value("${central.poll.backoff.base:2s}") Duration base,
      @Value("${central.poll.backoff.ceiling:60s}") Duration ceiling) {
    return new IntegrationBackoff(base, ceiling);
  }
}
