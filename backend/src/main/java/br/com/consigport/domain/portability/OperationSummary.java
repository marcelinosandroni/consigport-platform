package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.BeneficiaryId;
import java.time.Instant;

/**
 * Read model of one operation in the list of the consultations.
 *
 * <p>The aggregate {@link PortabilityOperation} carries the lifecycle of the operation,
 * not the bookkeeping of the storage: the instant of the last write of the row belongs
 * to the row, so the consultation reads it as a read model. The summary is what the
 * public API shows in the list: the identity, the protocol, the beneficiary, the status,
 * the total and the two instants of the life of the operation.
 *
 * @param id the identity of the operation
 * @param protocol the protocol of the Central, {@code null} while the operation was not
 *     accepted
 * @param beneficiaryId the beneficiary that owns the installments
 * @param status the current status
 * @param totalAmount the sum of the installments
 * @param createdAt the instant of the creation
 * @param updatedAt the instant of the last write of the operation
 */
public record OperationSummary(
    PortabilityOperation.OperationId id,
    String protocol,
    BeneficiaryId beneficiaryId,
    OperationStatus status,
    Money totalAmount,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Validates the read model.
   *
   * @param id the identity of the operation
   * @param beneficiaryId the beneficiary that owns the installments
   * @param status the current status
   * @param totalAmount the sum of the installments
   * @param createdAt the instant of the creation
   * @param updatedAt the instant of the last write of the operation
   * @throws IllegalArgumentException when any invariant is broken
   */
  public OperationSummary {
    if (id == null) {
      throw new IllegalArgumentException("operationId must not be null");
    }
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (totalAmount == null) {
      throw new IllegalArgumentException("totalAmount must not be null");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
    if (updatedAt == null) {
      throw new IllegalArgumentException("updatedAt must not be null");
    }
  }
}
