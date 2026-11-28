package br.com.consigport.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * The clock of the web slice of the tests.
 *
 * <p>The slice of the web loads only the controllers and the advice, so the clock of the
 * composition root is not there: the advice needs one to date the errors, and the test
 * needs a clock it can predict.
 */
@TestConfiguration(proxyBeanMethods = false)
public class FixedClockConfig {

  /** The instant that every error of the tests carries. */
  public static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");

  /**
   * The clock that never moves.
   *
   * @return the clock of the tests
   */
  @Bean
  public Clock clock() {
    return Clock.fixed(NOW, ZoneOffset.UTC);
  }
}
