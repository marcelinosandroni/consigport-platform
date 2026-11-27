package br.com.consigport.application.portability;

import br.com.consigport.domain.portability.OperationDetail;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.DomainError;

/**
 * Finds one portability operation by its identity.
 *
 * <p>The answer is the read model of the operation: the snapshot of the installments, the
 * protocol, the status, the reason of the rejection and the instant of the update of the
 * account.
 */
public final class FindOperation {

  private final PortabilityOperationRepository operations;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   */
  public FindOperation(PortabilityOperationRepository operations) {
    this.operations = operations;
  }

  /**
   * Finds the operation.
   *
   * @param id the identity of the operation
   * @return the detail of the operation
   * @throws DomainError.OperationNotFound when the operation does not exist
   */
  public OperationDetail execute(PortabilityOperation.OperationId id) {
    return operations
        .findDetailById(id)
        .orElseThrow(() -> new DomainError.OperationNotFound(id.value()));
  }
}
