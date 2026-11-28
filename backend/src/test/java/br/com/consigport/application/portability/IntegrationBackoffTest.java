package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * The backoff of the integration failures of an operation.
 *
 * <p>The wait doubles with every failure and never passes the ceiling of 60 s
 * ({@code ARCHITECTURE.md} §11): a Central that is down is not hammered.
 */
class IntegrationBackoffTest {

  private static final Duration BASE = Duration.ofSeconds(2);
  private static final Duration CEILING = Duration.ofSeconds(60);

  @Test
  void waitsTheBaseAfterTheFirstFailure() {
    assertThat(backoff().delayFor(1)).isEqualTo(Duration.ofSeconds(2));
  }

  @Test
  void doublesTheWaitAfterEveryFailure() {
    assertThat(backoff().delayFor(2)).isEqualTo(Duration.ofSeconds(4));
    assertThat(backoff().delayFor(3)).isEqualTo(Duration.ofSeconds(8));
    assertThat(backoff().delayFor(4)).isEqualTo(Duration.ofSeconds(16));
  }

  @Test
  void neverWaitsMoreThanTheCeiling() {
    assertThat(backoff().delayFor(6)).isEqualTo(CEILING);
    assertThat(backoff().delayFor(60)).isEqualTo(CEILING);
  }

  @Test
  void refusesANonPositiveBase() {
    assertThatThrownBy(() -> new IntegrationBackoff(Duration.ZERO, CEILING))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new IntegrationBackoff(Duration.ofSeconds(-1), CEILING))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refusesAMissingBase() {
    assertThatThrownBy(() -> new IntegrationBackoff(null, CEILING))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("base must be positive");
  }

  @Test
  void refusesAMissingCeiling() {
    assertThatThrownBy(() -> new IntegrationBackoff(BASE, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ceiling must not be shorter than the base");
  }

  @Test
  void waitsTheCeilingWhenTheMultiplicationOverflows() {
    IntegrationBackoff wide = new IntegrationBackoff(CEILING, CEILING);

    assertThat(wide.delayFor(33)).isEqualTo(CEILING);
  }

  @Test
  void refusesACeilingShorterThanTheBase() {
    assertThatThrownBy(() -> new IntegrationBackoff(BASE, Duration.ofSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refusesANumberOfFailuresThatIsNotPositive() {
    assertThatThrownBy(() -> backoff().delayFor(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static IntegrationBackoff backoff() {
    return new IntegrationBackoff(BASE, CEILING);
  }
}
