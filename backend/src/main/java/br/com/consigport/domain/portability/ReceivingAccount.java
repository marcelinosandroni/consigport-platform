package br.com.consigport.domain.portability;

/**
 * Destination account of a portability operation (BR-06).
 *
 * <p>Bank, branch and account are mandatory and cannot be blank.
 */
public record ReceivingAccount(String bank, String branch, String account) {

  /**
   * Validates and normalizes the account.
   *
   * @param bank the bank code
   * @param branch the branch code
   * @param account the account number
   * @throws IllegalArgumentException when a field is blank or longer than the contract
   *     allows
   */
  public ReceivingAccount {
    bank = requireText(bank, "bank", 64);
    branch = requireText(branch, "branch", 32);
    account = requireText(account, "account", 64);
  }

  private static String requireText(String value, String field, int maxLength) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    String trimmed = value.trim();
    if (trimmed.length() > maxLength) {
      throw new IllegalArgumentException(field + " must have at most " + maxLength + " characters");
    }
    return trimmed;
  }
}
