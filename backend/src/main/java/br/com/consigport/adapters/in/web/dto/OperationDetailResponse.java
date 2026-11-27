package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.portability.OperationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Answer of the consultation of one operation.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}): the summary of the operation plus the
 * snapshot of the request, the creditor, the account that receives the portability, the
 * reason of the rejection, the count of the integration attempts and the instant of the
 * update of the account.
 *
 * @param operationId the identity of the operation
 * @param protocol the protocol of the Central, {@code null} while it is not accepted
 * @param beneficiaryId the beneficiary that owns the installments
 * @param status the current status of the operation
 * @param totalAmount the sum of the frozen installments
 * @param createdAt the instant of the creation
 * @param updatedAt the instant of the last write of the operation
 * @param agreementId the agreement of the installments
 * @param installments the frozen installments
 * @param assigneeCreditor the assignee creditor returned by the Central
 * @param receivingAccount the account that receives the portability
 * @param rejectionReason the reason of the rejection, {@code null} unless rejected
 * @param retryCount how many integration attempts already failed
 * @param accountUpdatedAt when the receiving account was updated
 */
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
    ReceivingAccountResponse receivingAccount,
    String rejectionReason,
    int retryCount,
    Instant accountUpdatedAt) {}
