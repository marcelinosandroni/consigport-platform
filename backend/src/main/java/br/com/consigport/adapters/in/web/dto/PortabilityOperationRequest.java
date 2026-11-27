package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * The request that starts a portability operation.
 *
 * @param beneficiaryId the beneficiary of the installments
 * @param agreementId the agreement of the installments
 * @param installmentIds the eligible installments
 * @param assigneeCreditor the creditor that receives the installments
 * @param receivingAccount the account that receives the installments
 */
public record PortabilityOperationRequest(
    @NotBlank @Size(max = 64) String beneficiaryId,
    @NotBlank @Size(max = 64) String agreementId,
    @NotEmpty List<@NotBlank @Size(max = 64) String> installmentIds,
    @NotBlank @Size(max = 255) String assigneeCreditor,
    @Valid @NotNull ReceivingAccount receivingAccount) {}
