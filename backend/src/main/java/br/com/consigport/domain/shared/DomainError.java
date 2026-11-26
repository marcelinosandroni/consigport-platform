package br.com.consigport.domain.shared;

/**
 * Error of the domain, with the stable code exposed by the public API.
 *
 * <p>The hierarchy is sealed: the errors known by the core are closed and exhaustive.
 * The codes are the ones of the public contract and are never translated.
 */
public sealed abstract class DomainError extends RuntimeException
    permits DomainError.AuthorizationRequired,
        DomainError.BeneficiaryNotFound,
        DomainError.IdempotencyConflict,
        DomainError.IdempotencyKeyRequired,
        DomainError.InstallmentNotEligible,
        DomainError.CentralUnavailable {

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

  /**
   * The idempotency key was already used with another payload (BR-07).
   */
  public static final class IdempotencyConflict extends DomainError {

    private static final long serialVersionUID = 1L;

    private final transient IdempotencyKey idempotencyKey;

    /**
     * Builds the error.
     *
     * @param idempotencyKey the key that was already used
     */
    public IdempotencyConflict(IdempotencyKey idempotencyKey) {
      super("idempotency key already used with another payload: " + idempotencyKey.value());
      this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String code() {
      return "IDEMPOTENCY_CONFLICT";
    }

    /**
     * The key that was already used.
     *
     * @return the key
     */
    public IdempotencyKey idempotencyKey() {
      return idempotencyKey;
    }
  }

  /**
   * The request came without the {@code Idempotency-Key} header (BR-07).
   */
  public static final class IdempotencyKeyRequired extends DomainError {

    private static final long serialVersionUID = 1L;

    /** Builds the error. */
    public IdempotencyKeyRequired() {
      super("the Idempotency-Key header is required");
    }

    @Override
    public String code() {
      return "IDEMPOTENCY_KEY_REQUIRED";
    }
  }

  /**
   * One of the requested installments is not eligible (BR-03, BR-04).
   *
   * <p>An installment is not eligible when the agenda does not know it, when it is
   * closed or when it belongs to another agreement.
   */
  public static final class InstallmentNotEligible extends DomainError {

    private static final long serialVersionUID = 1L;

    private final transient InstallmentId installmentId;

    /**
     * Builds the error.
     *
     * @param installmentId the installment that is not eligible
     */
    public InstallmentNotEligible(InstallmentId installmentId) {
      super("installment is not eligible: " + installmentId.value());
      this.installmentId = installmentId;
    }

    @Override
    public String code() {
      return "INSTALLMENT_NOT_ELIGIBLE";
    }

    /**
     * The installment that is not eligible.
     *
     * @return the installment
     */
    public InstallmentId installmentId() {
      return installmentId;
    }
  }

  /**
   * The Central is unavailable, so the operation cannot be started (BR-08).
   */
  public static final class CentralUnavailable extends DomainError {

    private static final long serialVersionUID = 1L;

    /** Builds the error. */
    public CentralUnavailable() {
      super("the Central is unavailable");
    }

    @Override
    public String code() {
      return "CENTRAL_UNAVAILABLE";
    }
  }
}
