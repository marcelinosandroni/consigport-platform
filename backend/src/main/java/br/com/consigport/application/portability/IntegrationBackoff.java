package br.com.consigport.application.portability;

import java.time.Duration;

/**
 * Backoff of the integration failures of an operation.
 *
 * <p>The wait doubles with every failure and never passes the ceiling of 60 s
 * ({@code ARCHITECTURE.md} §11): the operation stays resumable and is retried at most
 * every minute, so a Central that is down is not hammered.
 */
public final class IntegrationBackoff {

  private final Duration base;
  private final Duration ceiling;

  /**
   * Builds the policy.
   *
   * @param base the wait of the first failure
   * @param ceiling the longest wait between two attempts
   */
  public IntegrationBackoff(Duration base, Duration ceiling) {
    if (base == null || base.isNegative() || base.isZero()) {
      throw new IllegalArgumentException("base must be positive");
    }
    if (ceiling == null || ceiling.compareTo(base) < 0) {
      throw new IllegalArgumentException("ceiling must not be shorter than the base");
    }
    this.base = base;
    this.ceiling = ceiling;
  }

  /**
   * The wait before the next attempt, given the failures already registered.
   *
   * @param failures how many times the integration failed
   * @return the wait before the next attempt
   */
  public Duration delayFor(int failures) {
    if (failures < 1) {
      throw new IllegalArgumentException("failures must be positive");
    }
    long multiplier = 1L << Math.min(failures - 1, 32);
    Duration delay;
    try {
      delay = base.multipliedBy(multiplier);
    } catch (ArithmeticException exception) {
      return ceiling;
    }
    return delay.compareTo(ceiling) > 0 ? ceiling : delay;
  }

  /**
   * The wait of the first failure.
   *
   * @return the base of the backoff
   */
  public Duration base() {
    return base;
  }

  /**
   * The longest wait between two attempts.
   *
   * @return the ceiling of the backoff
   */
  public Duration ceiling() {
    return ceiling;
  }
}
