package br.com.consigport.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the authorization aggregate.
 *
 * <p>The aggregate is immutable and rejects missing data at construction; the identity is
 * a UUID that the aggregate creates when the opt-in is registered.
 */
class AuthorizationTest {

  private static final Instant CREATED_AT = Instant.parse("2026-11-26T10:15:30Z");

  private static final BeneficiaryId BENEFICIARY = new BeneficiaryId("BEN-001");
  private static final AgreementId AGREEMENT = new AgreementId("AGR-001");
  private static final Authorization.AuthorizationId IDENTITY =
      new Authorization.AuthorizationId(UUID.fromString("3f0b0d3e-8a6d-4a8f-9b2f-6f1d2c3b4a50"));

  @Test
  void registersTheOptInWithTheGivenData() {
    Authorization authorization = Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT);

    assertThat(authorization.beneficiaryId()).isEqualTo(BENEFICIARY);
    assertThat(authorization.agreementId()).isEqualTo(AGREEMENT);
    assertThat(authorization.createdAt()).isEqualTo(CREATED_AT);
    assertThat(authorization.id().value()).isNotNull();
  }

  @Test
  void givesEveryOptInItsOwnIdentity() {
    Authorization first = Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT);
    Authorization second = Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT);

    assertThat(first.id()).isNotEqualTo(second.id());
  }

  @Test
  void comparesByValue() {
    Authorization authorization = Authorization.register(BENEFICIARY, AGREEMENT, CREATED_AT);
    Authorization same =
        new Authorization(
            authorization.id(), BENEFICIARY, AGREEMENT, CREATED_AT);

    assertThat(authorization).isEqualTo(same);
    assertThat(authorization)
        .isNotEqualTo(
            new Authorization(
                authorization.id(), new BeneficiaryId("BEN-002"), AGREEMENT, CREATED_AT));
  }

  @Test
  void rejectsAMissingIdentity() {
    assertThatThrownBy(() -> new Authorization(null, BENEFICIARY, AGREEMENT, CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("authorizationId must not be null");
  }

  @Test
  void rejectsAMissingBeneficiary() {
    assertThatThrownBy(() -> new Authorization(IDENTITY, null, AGREEMENT, CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("beneficiaryId must not be null");
  }

  @Test
  void rejectsAMissingAgreement() {
    assertThatThrownBy(() -> new Authorization(IDENTITY, BENEFICIARY, null, CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("agreementId must not be null");
  }

  @Test
  void rejectsAMissingCreationInstant() {
    assertThatThrownBy(() -> new Authorization(IDENTITY, BENEFICIARY, AGREEMENT, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("createdAt must not be null");
  }

  @Nested
  class AuthorizationIdTest {

    private static final String UUID_TEXT = "3f0b0d3e-8a6d-4a8f-9b2f-6f1d2c3b4a50";

    @Test
    void parsesAUuid() {
      assertThat(Authorization.AuthorizationId.parse(UUID_TEXT).value().toString())
          .isEqualTo(UUID_TEXT);
    }

    @Test
    void printsTheCanonicalText() {
      assertThat(Authorization.AuthorizationId.parse(UUID_TEXT)).hasToString(UUID_TEXT);
    }

    @Test
    void rejectsBlankIdentities() {
      assertThatThrownBy(() -> Authorization.AuthorizationId.parse(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("authorizationId must not be blank");
      assertThatThrownBy(() -> Authorization.AuthorizationId.parse("  "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("authorizationId must not be blank");
    }

    @Test
    void rejectsIdentitiesThatAreNotUuids() {
      assertThatThrownBy(() -> Authorization.AuthorizationId.parse("not-a-uuid"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("authorizationId must be a UUID");
    }

    @Test
    void rejectsANullUuid() {
      assertThatThrownBy(() -> new Authorization.AuthorizationId(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("authorizationId must not be null");
    }
  }
}
