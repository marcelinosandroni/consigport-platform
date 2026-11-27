package br.com.consigport.adapters.in.web.dto;

/**
 * Answer of the account that receives the portability.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}).
 *
 * @param bank the bank of the account
 * @param branch the branch of the account
 * @param account the number of the account
 */
public record ReceivingAccountResponse(String bank, String branch, String account) {}
