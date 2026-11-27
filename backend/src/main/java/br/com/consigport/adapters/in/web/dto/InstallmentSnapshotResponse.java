package br.com.consigport.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Answer of the snapshot of one installment frozen at the moment of the request.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}).
 *
 * @param installmentId the identity of the installment
 * @param agreementId the agreement the installment belongs to
 * @param amount the frozen amount
 * @param discountDate the frozen discount date
 */
public record InstallmentSnapshotResponse(
    String installmentId, String agreementId, BigDecimal amount, LocalDate discountDate) {}
