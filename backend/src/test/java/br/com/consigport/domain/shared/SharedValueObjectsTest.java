package br.com.consigport.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the shared value objects.
 *
 * <p>Every value object validates in the constructor and compares by value; the invalid
 * cases are part of the contract, not an afterthought.
 */
class SharedValueObjectsTest {

  @Nested
  class BeneficiaryIdTest {

    @Test
    void keepsTheIdentifierTrimmed() {
      assertThat(new BeneficiaryId(" BEN-001 ").value()).isEqualTo("BEN-001");
    }

    @Test
    void comparesByValue() {
      assertThat(new BeneficiaryId("BEN-001")).isEqualTo(new BeneficiaryId("BEN-001"));
      assertThat(new BeneficiaryId("BEN-001")).isNotEqualTo(new BeneficiaryId("BEN-002"));
    }

    @Test
    void rejectsBlankIdentifiers() {
      assertThatThrownBy(() -> new BeneficiaryId(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be blank");
      assertThatThrownBy(() -> new BeneficiaryId("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must not be blank");
    }

    @Test
    void rejectsIdentifiersLongerThanSixtyFourCharacters() {
      assertThatThrownBy(() -> new BeneficiaryId("B".repeat(65)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("beneficiaryId must have at most 64 characters");
    }
  }

  @Nested
  class AgreementIdTest {

    @Test
    void keepsTheIdentifierTrimmed() {
      assertThat(new AgreementId(" AGR-001 ").value()).isEqualTo("AGR-001");
    }

    @Test
    void comparesByValue() {
      assertThat(new AgreementId("AGR-001")).isEqualTo(new AgreementId("AGR-001"));
      assertThat(new AgreementId("AGR-001")).isNotEqualTo(new AgreementId("AGR-002"));
    }

    @Test
    void rejectsBlankIdentifiers() {
      assertThatThrownBy(() -> new AgreementId(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must not be blank");
      assertThatThrownBy(() -> new AgreementId(""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must not be blank");
    }

    @Test
    void rejectsIdentifiersLongerThanSixtyFourCharacters() {
      assertThatThrownBy(() -> new AgreementId("A".repeat(65)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("agreementId must have at most 64 characters");
    }
  }

  @Nested
  class InstallmentIdTest {

    @Test
    void keepsTheIdentifierTrimmed() {
      assertThat(new InstallmentId(" PAR-001 ").value()).isEqualTo("PAR-001");
    }

    @Test
    void comparesByValue() {
      assertThat(new InstallmentId("PAR-001")).isEqualTo(new InstallmentId("PAR-001"));
      assertThat(new InstallmentId("PAR-001")).isNotEqualTo(new InstallmentId("PAR-002"));
    }

    @Test
    void rejectsBlankIdentifiers() {
      assertThatThrownBy(() -> new InstallmentId(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentId must not be blank");
      assertThatThrownBy(() -> new InstallmentId("\t"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentId must not be blank");
    }

    @Test
    void rejectsIdentifiersLongerThanSixtyFourCharacters() {
      assertThatThrownBy(() -> new InstallmentId("P".repeat(65)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("installmentId must have at most 64 characters");
    }
  }

  @Nested
  class PageTest {

    @Test
    void countsThePagesOfTheResultSet() {
      assertThat(new Page<>(List.of("a", "b"), 0, 20, 25).totalPages()).isEqualTo(2);
      assertThat(new Page<>(List.of("a"), 0, 20, 1).totalPages()).isEqualTo(1);
      assertThat(new Page<>(List.of(), 0, 20, 0).totalPages()).isZero();
    }

    @Test
    void knowsWhenThePageIsTheLastOne() {
      assertThat(new Page<>(List.of("a"), 0, 20, 25).isLast()).isFalse();
      assertThat(new Page<>(List.of("a"), 1, 20, 25).isLast()).isTrue();
      assertThat(new Page<>(List.of(), 0, 20, 0).isLast()).isTrue();
    }

    @Test
    void rejectsAPageBeforeTheFirstOne() {
      assertThatThrownBy(() -> new Page<>(List.of(), -1, 20, 0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("page must not be negative");
    }

    @Test
    void rejectsASizeThatFitsNoItem() {
      assertThatThrownBy(() -> new Page<>(List.of(), 0, 0, 0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("size must be positive");
    }

    @Test
    void rejectsMoreItemsThanTheSize() {
      assertThatThrownBy(() -> new Page<>(List.of("a", "b"), 0, 1, 2))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("items must not exceed size");
    }

    @Test
    void rejectsANegativeTotal() {
      assertThatThrownBy(() -> new Page<>(List.of(), 0, 20, -1))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("totalElements must not be negative");
    }

    @Test
    void rejectsAMissingList() {
      assertThatThrownBy(() -> new Page<>(null, 0, 20, 0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("items must not be null");
    }

    @Test
    void rejectsMoreItemsThanTheWholeResultSet() {
      assertThatThrownBy(() -> new Page<>(List.of("a", "b"), 0, 20, 1))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("items must not exceed totalElements");
    }

    @Test
    void freezesTheItems() {
      Page<String> page = new Page<>(List.of("a"), 0, 20, 1);

      assertThatThrownBy(() -> page.items().clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  class IdempotencyKeyTest {

    private static final String UUID_TEXT = "6f1a1b4e-0f9d-4d0a-9f37-3a1d1a2b3c4d";

    @Test
    void parsesAUuid() {
      assertThat(IdempotencyKey.parse(UUID_TEXT).value().toString()).isEqualTo(UUID_TEXT);
    }

    @Test
    void comparesByValue() {
      assertThat(IdempotencyKey.parse(UUID_TEXT)).isEqualTo(IdempotencyKey.parse(UUID_TEXT));
      assertThat(IdempotencyKey.parse(UUID_TEXT))
          .isNotEqualTo(IdempotencyKey.parse("00000000-0000-0000-0000-000000000000"));
    }

    @Test
    void printsTheCanonicalText() {
      assertThat(IdempotencyKey.parse(UUID_TEXT)).hasToString(UUID_TEXT);
    }

    @Test
    void rejectsBlankKeys() {
      assertThatThrownBy(() -> IdempotencyKey.parse(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("idempotencyKey must not be blank");
      assertThatThrownBy(() -> IdempotencyKey.parse("  "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("idempotencyKey must not be blank");
    }

    @Test
    void rejectsKeysThatAreNotUuids() {
      assertThatThrownBy(() -> IdempotencyKey.parse("not-a-uuid"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("idempotencyKey must be a UUID");
    }

    @Test
    void rejectsANullUuid() {
      assertThatThrownBy(() -> new IdempotencyKey(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("idempotencyKey must not be null");
    }
  }
}
