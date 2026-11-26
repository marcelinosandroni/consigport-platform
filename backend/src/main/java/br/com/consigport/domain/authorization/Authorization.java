package br.com.consigport.domain.authorization;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;
import java.util.UUID;

/**
 * Opt-in of a beneficiary for one agreement (BR-02).
 *
 * <p>The aggregate is immutable: it carries the identity, the beneficiary, the agreement
 * and the instant of the opt-in. It is unique per {@code (beneficiaryId, agreementId)};
 * that uniqueness is enforced by {@link AuthorizationRepository} and by the use case that
 * repeats the opt-in.
 *
 * <p>No framework type crosses this class: the boundary adapter maps it to the wire.
 */
public record Authorization(
    AuthorizationId id,
    BeneficiaryId beneficiaryId,
    AgreementId agreementId,
    Instant createdAt) {

  /**
   * Validates the aggregate.
   *
   * @param id the identity of the authorization
   * @param beneficiaryId the beneficiary that gave the opt-in
   * @param agreementId the agreement the opt-in covers
   * @param createdAt the instant of the opt-in
   * @throws IllegalArgumentException when any field is {@code null}
   */
  public Authorization {
    if (id == null) {
      throw new IllegalArgumentException("authorizationId must not be null");
    }
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    if (agreementId == null) {
      throw new IllegalArgumentException("agreementId must not be null");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
  }

  /**
   * Registers a new opt-in with a fresh identity.
   *
   * @param beneficiaryId the beneficiary that gave the opt-in
   * @param agreementId the agreement the opt-in covers
   * @param createdAt the instant of the opt-in, read by the use case from the clock
   * @return the new authorization
   */
  public static Authorization register(
      BeneficiaryId beneficiaryId, AgreementId agreementId, Instant createdAt) {
    return new Authorization(new AuthorizationId(UUID.randomUUID()), beneficiaryId, agreementId, createdAt);
  }

  /**
   * Identity of an authorization.
   *
   * @param value the UUID of the authorization
   */
  public record AuthorizationId(UUID value) {

    /**
     * Validates the identity.
     *
     * @param value the UUID of the authorization
     * @throws IllegalArgumentException when the UUID is {@code null}
     */
    public AuthorizationId {
      if (value == null) {
        throw new IllegalArgumentException("authorizationId must not be null");
      }
    }

    /**
     * Parses the identity from its textual form.
     *
     * @param value the raw UUID
     * @return the parsed identity
     * @throws IllegalArgumentException when the text is blank or is not a UUID
     */
    public static AuthorizationId parse(String value) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException("authorizationId must not be blank");
      }
      try {
        return new AuthorizationId(UUID.fromString(value.trim()));
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("authorizationId must be a UUID", exception);
      }
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }
}
