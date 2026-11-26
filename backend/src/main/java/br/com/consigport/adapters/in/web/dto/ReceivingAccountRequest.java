package br.com.consigport.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Destination account of a portability operation, as it arrives in the request.
 *
 * @param bank the bank code
 * @param branch the branch code
 * @param account the account number
 */
public record ReceivingAccountRequest(
    @NotBlank @Size(max = 64) String bank,
    @NotBlank @Size(max = 32) String branch,
    @NotBlank @Size(max = 64) String account) {}
