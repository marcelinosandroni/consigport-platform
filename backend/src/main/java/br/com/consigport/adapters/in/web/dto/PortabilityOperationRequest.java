package br.com.consigport.adapters.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Body of the request that starts a portability operation.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The request carries the choice of the
 * beneficiary; the amounts and the dates are read from the agenda and frozen by the
 * backend.
 *
 * @param beneficiaryId the beneficiary that owns the installments
 * @param agreementId the agreement the installments belong to
 * @param installmentIds the selected installments
 * @param assigneeCreditor the creditor that receives the installments
 * @param receivingAccount the account that receives the portability
 */
public record PortabilityOperationRequest(
    @NotBlank @Size(max = 64) String beneficiaryId,
    @NotBlank @Size(max = 64) String agreementId,
    @NotEmpty List<@NotBlank @Size(max = 64) String> installmentIds,
    @NotBlank @Size(max = 255) String assigneeCreditor,
    @Valid @NotNull ReceivingAccountRequest receivingAccount) {}
