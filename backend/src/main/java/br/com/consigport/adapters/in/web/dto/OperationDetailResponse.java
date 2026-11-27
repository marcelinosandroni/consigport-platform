package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.portability.OperationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The detail of an operation, as the public API answers it.
 *
 * @param operationId the identity of the operation
 * @param protocol the protocol of the Central
 * @param beneficiaryId the beneficiary of the operation
 * @param status the state of the operation
 * @param totalAmount the sum of the installments
 * @param createdAt the instant of the creation
 * @param updatedAt the instant of the last write
 * @param agreementId the agreement of the installments
 * @param installments the snapshot of the installments
 * @param assigneeCreditor the creditor that receives the installments
 * @param receivingAccount the account that receives the installments
 * @param rejectionReason the reason of the rejection, when the Central rejects
 * @param retryCount the attempts of integration already made
 * @param accountUpdatedAt the instant of the update of the account
 */
@Schema(
    name = "OperationDetail",
    requiredProperties = {
      "operationId",
      "protocol",
      "beneficiaryId",
      "status",
      "totalAmount",
      "createdAt",
      "updatedAt",
      "agreementId",
      "installments",
      "assigneeCreditor",
      "receivingAccount",
      "rejectionReason",
      "retryCount",
      "accountUpdatedAt"
    })
public record OperationDetailResponse(
    String operationId,
    String protocol,
    String beneficiaryId,
    OperationStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt,
    String agreementId,
    List<InstallmentSnapshotResponse> installments,
    String assigneeCreditor,
    ReceivingAccount receivingAccount,
    String rejectionReason,
    int retryCount,
    Instant accountUpdatedAt) {}
