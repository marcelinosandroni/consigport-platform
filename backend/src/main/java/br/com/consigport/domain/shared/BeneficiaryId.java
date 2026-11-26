package br.com.consigport.domain.shared;

/**
 * Identifier of a beneficiary.
 *
 * <p>A strong type in place of a loose {@code String}: the value is validated once, at
 * the boundary, and every later comparison is by value.
 */
public record BeneficiaryId(String value) {

  private static final int MAX_LENGTH = 64;

  /**
   * Validates and normalizes the identifier.
   *
   * @param value the raw identifier
   * @throws IllegalArgumentException when the identifier is blank or longer than 64
   *     characters
   */
  public BeneficiaryId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("beneficiaryId must not be blank");
    }
    value = value.trim();
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("beneficiaryId must have at most 64 characters");
    }
  }
}
