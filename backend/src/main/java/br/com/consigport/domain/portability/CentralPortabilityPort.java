package br.com.consigport.domain.portability;

/**
 * Port of the submission of a portability operation to the Central.
 *
 * <p>The Central owns the confirmation of the operation: it answers {@code 202} with the
 * protocol and confirms later, asynchronously (BR-08). The port is idempotent on the
 * side of the Central: the same idempotency key with the same payload always answers
 * the same protocol, so a retry never duplicates the operation (BR-15).
 */
public interface CentralPortabilityPort {

  /**
   * Submits the operation to the Central.
   *
   * @param operation the operation to submit
   * @return the protocol returned by the Central
   * @throws CentralSubmissionException when the Central does not accept the operation
   */
  String submit(PortabilityOperation operation);
}
