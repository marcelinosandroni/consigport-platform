package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.portability.OperationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * The summary of an operation, as the list of the operations answers it.
 *
 * @param operationId the identity of the operation
 * @param protocol the protocol of the Central
 * @param beneficiaryId the beneficiary of the operation
 * @param status the state of the operation
 * @param totalAmount the sum of the installments
 * @param createdAt the instant of the creation
 * @param updatedAt the instant of the last write
 */
@Schema(
    name = "OperationSummary",
    requiredProperties = {
      "operationId",
      "protocol",
      "beneficiaryId",
      "status",
      "totalAmount",
      "createdAt",
      "updatedAt"
    })
public record OperationSummaryResponse(
    String operationId,
    String protocol,
    String beneficiaryId,
    OperationStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt) {}
