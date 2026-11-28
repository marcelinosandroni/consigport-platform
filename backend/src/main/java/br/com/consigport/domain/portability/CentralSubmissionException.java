package br.com.consigport.domain.portability;

/**
 * Raised when the Central does not accept the submission of an operation.
 *
 * <p>The failure is never fatal for the operation: it stays resumable and the worker
 * sends it again with the same idempotency key (BR-11, BR-15). The exception carries
 * the stable code of the Central, so the trail of the audit explains what the Central
 * answered without translating it.
 */
public final class CentralSubmissionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** Code used when the Central did not answer at all. */
  public static final String UNREACHABLE_CODE = "CENTRAL_INDISPONIVEL";

  private final transient int status;
  private final transient String code;

  /**
   * Builds the exception for the answer of the Central.
   *
   * @param status the HTTP status answered by the Central, {@code 0} when there was no
   *     answer
   * @param code the stable code of the Central
   * @param message the message of the failure
   */
  public CentralSubmissionException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code == null || code.isBlank() ? UNREACHABLE_CODE : code;
  }

  /**
   * Builds the exception for a Central that did not answer.
   *
   * @param message the message of the failure
   */
  public CentralSubmissionException(String message) {
    this(0, UNREACHABLE_CODE, message);
  }

  /**
   * The HTTP status answered by the Central.
   *
   * @return the status, {@code 0} when there was no answer
   */
  public int status() {
    return status;
  }

  /**
   * The stable code of the Central.
   *
   * @return the code
   */
  public String code() {
    return code;
  }
}
