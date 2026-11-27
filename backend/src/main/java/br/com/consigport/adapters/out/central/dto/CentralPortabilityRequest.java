package br.com.consigport.adapters.out.central.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Operation of portability as the Central receives it, in Portuguese.
 *
 * <p>This DTO belongs to the outbound adapter and never leaves it: the domain speaks
 * English and the anti-corruption layer translates the contract of the Central.
 *
 * <p>The {@code assinatura} field is the mirror of the {@code X-Signature} header. It is
 * absent while the body is canonicalized for the signature, because the signature never
 * takes part in what it signs.
 *
 * @param beneficiarioId the beneficiary that owns the installments
 * @param parcelas the identifiers of the eligible installments
 * @param credorCessionario the creditor that receives the installments
 * @param contaRecebimento the destination account
 * @param assinatura the mirror of the signature header, {@code null} before the signing
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CentralPortabilityRequest(
    String beneficiarioId,
    List<String> parcelas,
    String credorCessionario,
    ContaRecebimento contaRecebimento,
    String assinatura) {

  /**
   * Destination account as the Central writes it.
   *
   * @param banco the bank code
   * @param agencia the branch code
   * @param conta the account number
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ContaRecebimento(String banco, String agencia, String conta) {}
}
