package br.com.consigport.adapters.out.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Installment as it is stored in the {@code installments} column of the operations.
 *
 * <p>The shape is the one of the snapshot of {@code ARCHITECTURE.md} §6. The date is
 * text, not {@code LocalDate}, so the JSON never depends on the modules registered in
 * the object mapper of the persistence layer.
 *
 * @param installmentId the identifier of the installment
 * @param agreementId the agreement of the installment
 * @param amount the frozen amount
 * @param discountDate the frozen discount date, as {@code yyyy-MM-dd}
 */
record InstallmentSnapshotRow(
    @JsonProperty("installmentId") String installmentId,
    @JsonProperty("agreementId") String agreementId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("discountDate") String discountDate) {}
