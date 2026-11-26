package br.com.consigport.domain.portability;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary amount of an installment.
 *
 * <p>Two decimal places and never negative: the scale is normalized, so {@code Money(10)}
 * and {@code Money(10.00)} are the same value and compare equal.
 */
public record Money(BigDecimal amount) {

  private static final int SCALE = 2;

  /**
   * Validates and normalizes the amount.
   *
   * @param amount the raw amount
   * @throws IllegalArgumentException when the amount is {@code null}, negative or has more
   *     than two decimal places
   */
  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("amount must not be null");
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("amount must not be negative");
    }
    if (amount.scale() > SCALE) {
      throw new IllegalArgumentException("amount must have at most 2 decimal places");
    }
    amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
  }
}
