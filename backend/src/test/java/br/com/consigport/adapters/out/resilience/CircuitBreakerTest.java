package br.com.consigport.adapters.out.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the circuit that protects the calls to a dependency that is failing.
 *
 * <p>The clock is controlled, so the window, the opening, the ten seconds open and the
 * three calls in half-open state are proven without waiting.
 */
class CircuitBreakerTest {

  private static final Instant NOW = Instant.parse("2026-11-27T10:15:30Z");

  private MutableClock clock;
  private CircuitBreaker circuit;

  @BeforeEach
  void setUp() {
    clock = new MutableClock(NOW);
    circuit = new CircuitBreaker(10, 0.5, Duration.ofSeconds(10), 3, clock);
  }

  @Test
  void letsEveryCallThroughWhileTheCircuitIsClosed() {
    assertThat(circuit.allowsCall()).isTrue();
    assertThat(circuit.isOpen()).isFalse();
  }

  @Test
  void opensTheCircuitWhenHalfOfTheWindowFailed() {
    for (int attempt = 0; attempt < 5; attempt++) {
      circuit.recordSuccess();
    }
    for (int attempt = 0; attempt < 5; attempt++) {
      circuit.recordFailure();
    }

    assertThat(circuit.isOpen()).isTrue();
    assertThat(circuit.allowsCall()).isFalse();
  }

  @Test
  void keepsTheCircuitClosedWhileTheFailuresAreFew() {
    for (int attempt = 0; attempt < 9; attempt++) {
      circuit.recordSuccess();
    }
    circuit.recordFailure();

    assertThat(circuit.isOpen()).isFalse();
    assertThat(circuit.allowsCall()).isTrue();
  }

  @Test
  void doesNotCountTheFailuresThatLeftTheWindow() {
    failFourTimesAndSucceedSixTimes();

    assertThat(circuit.isOpen()).isFalse();

    failFourTimesAndSucceedSixTimes();

    assertThat(circuit.isOpen()).isFalse();
  }

  private void failFourTimesAndSucceedSixTimes() {
    for (int attempt = 0; attempt < 4; attempt++) {
      circuit.recordFailure();
    }
    for (int attempt = 0; attempt < 6; attempt++) {
      circuit.recordSuccess();
    }
  }

  @Test
  void refusesTheCallsWhileTheCircuitIsOpen() {
    openTheCircuit();

    assertThat(circuit.allowsCall()).isFalse();
    assertThat(circuit.allowsCall()).isFalse();
  }

  @Test
  void letsTheCallsThroughAgainAfterTheOpenDuration() {
    openTheCircuit();
    clock.advance(Duration.ofSeconds(10));

    assertThat(circuit.allowsCall()).isTrue();
    assertThat(circuit.isOpen()).isFalse();
  }

  @Test
  void closesTheCircuitAfterThreeCallsInHalfOpenState() {
    openTheCircuit();
    clock.advance(Duration.ofSeconds(10));

    assertThat(circuit.allowsCall()).isTrue();
    circuit.recordSuccess();
    circuit.recordSuccess();
    circuit.recordSuccess();

    assertThat(circuit.isOpen()).isFalse();
    assertThat(circuit.allowsCall()).isTrue();
  }

  @Test
  void opensTheCircuitAgainWhenAhalfOpenCallFails() {
    openTheCircuit();
    clock.advance(Duration.ofSeconds(10));

    assertThat(circuit.allowsCall()).isTrue();
    circuit.recordFailure();

    assertThat(circuit.isOpen()).isTrue();
    assertThat(circuit.allowsCall()).isFalse();
  }

  private void openTheCircuit() {
    for (int attempt = 0; attempt < 5; attempt++) {
      circuit.recordSuccess();
    }
    for (int attempt = 0; attempt < 5; attempt++) {
      circuit.recordFailure();
    }
  }
}
