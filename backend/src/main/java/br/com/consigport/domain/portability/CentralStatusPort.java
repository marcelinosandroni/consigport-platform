package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.DomainError;

/**
 * Port of the read of the situation of an operation in the Central.
 *
 * <p>Declared in the domain and implemented by the outbound adapter that speaks the
 * contract of the Central (ADR {@code 0003}). The answer is always one of the three
 * shapes of {@link CentralStatus}: the adapter translates {@code 204}, {@code 200
 * CONFIRMADA} and {@code 200 REJEITADA}.
 */
public interface CentralStatusPort {

  /**
   * Reads the situation of the operation in the Central.
   *
   * @param protocol the protocol returned by the Central when the operation was accepted
   * @return the situation of the operation
   * @throws DomainError.CentralUnavailable when the Central does not answer
   */
  CentralStatus statusOf(String protocol);
}
