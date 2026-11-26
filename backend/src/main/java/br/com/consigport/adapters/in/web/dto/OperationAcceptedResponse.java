package br.com.consigport.adapters.in.web.dto;

import br.com.consigport.domain.portability.OperationStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Answer of the request that starts a portability operation.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The protocol is {@code null} while the
 * Central has not accepted the operation.
 *
 * @param operationId the identity of the operation
 * @param protocol the protocol of the Central, {@code null} while it is not accepted
 * @param status the current status of the operation
 * @param totalAmount the sum of the frozen installments
 * @param createdAt the instant of the creation
 */
public record OperationAcceptedResponse(
    String operationId, String protocol, OperationStatus status, BigDecimal totalAmount, Instant createdAt) {}
