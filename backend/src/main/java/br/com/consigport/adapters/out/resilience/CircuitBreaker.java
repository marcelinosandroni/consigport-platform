package br.com.consigport.adapters.out.resilience;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Circuit that protects the calls to a dependency that is failing.
 *
 * <p>The values come from {@code ARCHITECTURE.md} §11: a window of 10 calls, 50% of
 * failures open the circuit, the circuit stays open for 10 s and three calls in half-open
 * state close it again. A failure in half-open state opens it once more.
 *
 * <p>The circuit is closed by default: the calls go through and their outcome is
 * remembered in the window. The clock is injected, so the behaviour of the circuit is
 * proven with a controlled clock.
 */
public final class CircuitBreaker {

  /** The state of the circuit. */
  private enum State {
    CLOSED,
    OPEN,
    HALF_OPEN
  }

  private final int windowSize;
  private final double failureThreshold;
  private final Duration openDuration;
  private final int halfOpenCalls;
  private final Clock clock;

  private final Deque<Boolean> outcomes = new ArrayDeque<>();
  private State state = State.CLOSED;
  private Instant openedAt;
  private int halfOpenSuccesses;

  /**
   * Builds the circuit.
   *
   * @param windowSize how many outcomes the window remembers
   * @param failureThreshold the ratio of failures that opens the circuit, between 0 and 1
   * @param openDuration how long the circuit stays open
   * @param halfOpenCalls how many calls in half-open state close the circuit
   * @param clock the clock that measures the open duration
   */
  public CircuitBreaker(
      int windowSize,
      double failureThreshold,
      Duration openDuration,
      int halfOpenCalls,
      Clock clock) {
    if (windowSize < 1) {
      throw new IllegalArgumentException("windowSize must be positive");
    }
    if (failureThreshold <= 0 || failureThreshold > 1) {
      throw new IllegalArgumentException("failureThreshold must be between 0 and 1");
    }
    if (openDuration == null || openDuration.isNegative()) {
      throw new IllegalArgumentException("openDuration must not be negative");
    }
    if (halfOpenCalls < 1) {
      throw new IllegalArgumentException("halfOpenCalls must be positive");
    }
    this.windowSize = windowSize;
    this.failureThreshold = failureThreshold;
    this.openDuration = openDuration;
    this.halfOpenCalls = halfOpenCalls;
    this.clock = clock;
  }

  /**
   * Tells whether the call may go through.
   *
   * @return {@code true} when the dependency may be called
   */
  public synchronized boolean allowsCall() {
    if (state == State.OPEN && !openDurationElapsed()) {
      return false;
    }
    if (state == State.OPEN) {
      state = State.HALF_OPEN;
      halfOpenSuccesses = 0;
    }
    return true;
  }

  /**
   * Registers the outcome of a call that went through.
   *
   * <p>A circuit that was open and whose duration has elapsed accepts the outcome as a
   * call in half-open state: the call that closed the open period is the first probe of
   * the dependency.
   *
   * @param succeeded whether the call succeeded
   */
  public synchronized void record(boolean succeeded) {
    if (state == State.OPEN && openDurationElapsed()) {
      state = State.HALF_OPEN;
      halfOpenSuccesses = 0;
    }
    if (state == State.HALF_OPEN) {
      if (succeeded) {
        halfOpenSuccesses++;
        if (halfOpenSuccesses >= halfOpenCalls) {
          close();
        }
        return;
      }
      open();
      return;
    }
    outcomes.addLast(succeeded);
    while (outcomes.size() > windowSize) {
      outcomes.removeFirst();
    }
    if (state == State.CLOSED && shouldOpen()) {
      open();
    }
  }

  /**
   * Registers a successful call.
   */
  public void recordSuccess() {
    record(true);
  }

  /**
   * Registers a failed call.
   */
  public void recordFailure() {
    record(false);
  }

  /**
   * Whether the circuit is refusing the calls.
   *
   * @return {@code true} when the circuit is open
   */
  public synchronized boolean isOpen() {
    return state == State.OPEN;
  }

  private boolean openDurationElapsed() {
    return !Instant.now(clock).isBefore(openedAt.plus(openDuration));
  }

  private boolean shouldOpen() {
    if (outcomes.size() < windowSize) {
      return false;
    }
    long failures = outcomes.stream().filter(failed -> !failed).count();
    return (double) failures / outcomes.size() >= failureThreshold;
  }

  private void open() {
    state = State.OPEN;
    openedAt = Instant.now(clock);
    halfOpenSuccesses = 0;
  }

  private void close() {
    state = State.CLOSED;
    outcomes.clear();
    openedAt = null;
    halfOpenSuccesses = 0;
  }
}
