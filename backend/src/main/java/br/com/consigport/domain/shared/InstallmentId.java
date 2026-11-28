package br.com.consigport.domain.shared;

/**
 * Identifier of an installment of a payroll deduction.
 *
 * <p>A strong type in place of a loose {@code String}: the value is validated once, at
 * the boundary, and every later comparison is by value.
 */
public record InstallmentId(String value) {

  private static final int MAX_LENGTH = 64;

  /**
   * Validates and normalizes the identifier.
   *
   * @param value the raw identifier
   * @throws IllegalArgumentException when the identifier is blank or longer than 64
   *     characters
   */
  public InstallmentId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("installmentId must not be blank");
    }
    value = value.trim();
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("installmentId must have at most 64 characters");
    }
  }
}
