package br.com.consigport.domain.shared;

import java.util.UUID;

/**
 * Idempotency key of a portability submission (BR-07).
 *
 * <p>The key is a UUID: any other text is rejected at the boundary, so a retry of the
 * same submission always carries the same key.
 */
public record IdempotencyKey(UUID value) {

  /**
   * Validates the key.
   *
   * @param value the parsed UUID
   * @throws IllegalArgumentException when the key is {@code null}
   */
  public IdempotencyKey {
    if (value == null) {
      throw new IllegalArgumentException("idempotencyKey must not be null");
    }
  }

  /**
   * Parses the key from its textual form.
   *
   * @param value the raw key
   * @return the parsed key
   * @throws IllegalArgumentException when the text is blank or is not a UUID
   */
  public static IdempotencyKey parse(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("idempotencyKey must not be blank");
    }
    try {
      return new IdempotencyKey(UUID.fromString(value.trim()));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("idempotencyKey must be a UUID", exception);
    }
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
