package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.portability.OperationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * The operation accepted by the Central, as the public API answers it.
 *
 * @param operationId the identity of the operation
 * @param protocol the protocol of the Central, null until the Central accepts it
 * @param status the state of the operation
 * @param totalAmount the sum of the installments
 * @param createdAt the instant of the creation
 */
@Schema(
    name = "OperationAcceptedResponse",
    requiredProperties = {"operationId", "protocol", "status", "totalAmount", "createdAt"})
public record OperationAcceptedResponse(
    String operationId, String protocol, OperationStatus status, BigDecimal totalAmount, Instant createdAt) {}
