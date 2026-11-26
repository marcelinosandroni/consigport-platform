package br.com.consigport.domain.shared;

/**
 * Error of the domain, with the stable code exposed by the public API.
 *
 * <p>The hierarchy is sealed: the errors known by the core are closed and exhaustive.
 * The codes are the ones of the public contract and are never translated.
 */
public sealed abstract class DomainError extends RuntimeException
    permits DomainError.AuthorizationRequired, DomainError.BeneficiaryNotFound {

  private static final long serialVersionUID = 1L;

  /**
   * Builds the error.
   *
   * @param message the message of the error
   */
  protected DomainError(String message) {
    super(message);
  }

  /**
   * The stable code of the error, as exposed by the public API.
   *
   * @return the code of the error
   */
  public abstract String code();

  /**
   * The beneficiary has no opt-in, so the agenda cannot be consulted (BR-01).
   */
  public static final class AuthorizationRequired extends DomainError {

    private static final long serialVersionUID = 1L;

    /** Builds the error. */
    public AuthorizationRequired() {
      super("the beneficiary has no authorization to consult the agenda");
    }

    @Override
    public String code() {
      return "AUTHORIZATION_REQUIRED";
    }
  }

  /**
   * The Central does not know the beneficiary.
   */
  public static final class BeneficiaryNotFound extends DomainError {

    private static final long serialVersionUID = 1L;

    private final transient BeneficiaryId beneficiaryId;

    /**
     * Builds the error.
     *
     * @param beneficiaryId the unknown beneficiary
     */
    public BeneficiaryNotFound(BeneficiaryId beneficiaryId) {
      super("beneficiary not found: " + beneficiaryId.value());
      this.beneficiaryId = beneficiaryId;
    }

    @Override
    public String code() {
      return "BENEFICIARY_NOT_FOUND";
    }

    /**
     * The unknown beneficiary.
     *
     * @return the beneficiary
     */
    public BeneficiaryId beneficiaryId() {
      return beneficiaryId;
    }
  }
}
