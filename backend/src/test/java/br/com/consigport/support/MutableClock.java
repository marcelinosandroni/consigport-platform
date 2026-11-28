package br.com.consigport.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Clock that moves only when the test says so.
 *
 * <p>The polling, the backoff and the circuit are all dated by a clock: a controlled
 * clock proves their behaviour without waiting and without flakiness.
 */
public final class MutableClock extends Clock {

  private Instant instant;
  private final ZoneId zone;

  /**
   * Builds the clock.
   *
   * @param instant the instant the clock starts at
   */
  public MutableClock(Instant instant) {
    this(instant, ZoneOffset.UTC);
  }

  /**
   * Builds the clock.
   *
   * @param instant the instant the clock starts at
   * @param zone the zone of the clock
   */
  public MutableClock(Instant instant, ZoneId zone) {
    this.instant = instant;
    this.zone = zone;
  }

  /**
   * Moves the clock forward.
   *
   * @param duration how much the clock moves
   */
  public void advance(Duration duration) {
    instant = instant.plus(duration);
  }

  /**
   * Moves the clock to an instant.
   *
   * @param instant the instant the clock moves to
   */
  public void moveTo(Instant instant) {
    this.instant = instant;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return new MutableClock(instant, zone);
  }

  @Override
  public Instant instant() {
    return instant;
  }
}
