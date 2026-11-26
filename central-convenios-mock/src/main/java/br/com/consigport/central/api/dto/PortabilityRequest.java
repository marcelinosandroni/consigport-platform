package br.com.consigport.central.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Portability request of {@code POST /operacoes-portabilidade}.
 *
 * @param beneficiarioId beneficiary that owns the installments
 * @param parcelas identifiers of the eligible installments
 * @param credorCessionario creditor that starts receiving the installments
 * @param contaRecebimento destination account
 * @param assinatura mirror of the {@code X-Signature} header, validated in task 1.6
 */
public record PortabilityRequest(
    @NotBlank @Size(max = 64) String beneficiarioId,
    @NotEmpty List<@NotBlank String> parcelas,
    @NotBlank @Size(max = 255) String credorCessionario,
    @NotNull @Valid ContaRecebimento contaRecebimento,
    @NotBlank String assinatura) {

  /** Destination account of the portability ({@code ContaRecebimento}). */
  public record ContaRecebimento(
      @NotBlank @Size(max = 64) String banco,
      @NotBlank @Size(max = 32) String agencia,
      @NotBlank @Size(max = 64) String conta) {}
}
