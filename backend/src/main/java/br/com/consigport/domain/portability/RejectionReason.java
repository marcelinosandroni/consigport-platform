package br.com.consigport.domain.portability;

/**
 * Reason returned by the Central when the operation is rejected (BR-11).
 *
 * <p>The hierarchy is sealed: the reasons known by the domain are closed and exhaustive,
 * so a switch over them never needs a default branch. The ACL translates the Portuguese
 * reason of the Central into the English code of the domain.
 */
public sealed interface RejectionReason permits RejectionReason.MarginInsufficient {

  /**
   * Stable English code of the reason, as exposed by the public API.
   *
   * @return the code of the reason
   */
  String code();

  /**
   * Resolves a reason from its English code.
   *
   * @param code the code of the reason
   * @return the matching reason
   * @throws IllegalArgumentException when the code is unknown
   */
  static RejectionReason fromCode(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("rejection reason code must not be blank");
    }
    return switch (code.trim()) {
      case "MARGIN_INSUFFICIENT" -> new MarginInsufficient();
      default -> throw new IllegalArgumentException("unknown rejection reason: " + code);
    };
  }

  /**
   * The consignee margin is not enough to carry the installments (BR-14).
   */
  record MarginInsufficient() implements RejectionReason {

    @Override
    public String code() {
      return "MARGIN_INSUFFICIENT";
    }
  }
}
