package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The snapshot of one installment, frozen when the operation starts.
 *
 * @param installmentId the identity of the installment
 * @param agreementId the agreement of the installment
 * @param amount the amount of the installment
 * @param discountDate the date of the discount
 */
@Schema(
    name = "InstallmentSnapshot",
    requiredProperties = {"installmentId", "agreementId", "amount", "discountDate"})
public record InstallmentSnapshotResponse(
    String installmentId, String agreementId, BigDecimal amount, LocalDate discountDate) {}
