package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The receiving account of the installments, as the request asks for it and as the
 * answer carries it.
 *
 * <p>The contract publishes one schema for the two directions: the fields are the same,
 * and the constraints of the request hold for the answer as well.
 *
 * @param bank the bank of the account
 * @param branch the branch of the account
 * @param account the number of the account
 */
@Schema(name = "ReceivingAccount")
public record ReceivingAccount(
    @NotBlank @Size(max = 64) String bank,
    @NotBlank @Size(max = 32) String branch,
    @NotBlank @Size(max = 64) String account) {}
