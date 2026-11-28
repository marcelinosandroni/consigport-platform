package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Vocabulary of the rejection reasons.
 *
 * <p>The reasons are sealed and exhaustive: the English code is the one exposed by the
 * public API and the one the ACL translates from the Portuguese reason of the Central.
 */
class RejectionReasonTest {

  @Test
  void exposesTheEnglishCode() {
    assertThat(new RejectionReason.MarginInsufficient().code()).isEqualTo("MARGIN_INSUFFICIENT");
  }

  @Test
  void exposesTheCodeOfTheReasonTheDomainDoesNotMap() {
    assertThat(new RejectionReason.Unmapped().code()).isEqualTo("UNMAPPED_REJECTION");
  }

  @Test
  void resolvesAReasonFromItsCode() {
    assertThat(RejectionReason.fromCode("MARGIN_INSUFFICIENT"))
        .isInstanceOf(RejectionReason.MarginInsufficient.class);
  }

  @Test
  void resolvesTheUnmappedReasonFromItsCode() {
    assertThat(RejectionReason.fromCode("UNMAPPED_REJECTION"))
        .isInstanceOf(RejectionReason.Unmapped.class);
  }

  @Test
  void refusesAnUnknownCode() {
    assertThatThrownBy(() -> RejectionReason.fromCode("OUTER_SPACE"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("unknown rejection reason: OUTER_SPACE");
  }

  @Test
  void refusesABlankCode() {
    assertThatThrownBy(() -> RejectionReason.fromCode(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("rejection reason code must not be blank");
  }

  @Test
  void refusesAMissingCode() {
    assertThatThrownBy(() -> RejectionReason.fromCode(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("rejection reason code must not be blank");
  }
}
