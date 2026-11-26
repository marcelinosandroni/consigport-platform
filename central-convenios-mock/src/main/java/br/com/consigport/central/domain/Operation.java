package br.com.consigport.central.domain;

import java.time.Instant;
import java.util.List;

/**
 * A portability operation accepted by the Central.
 *
 * <p>Instances are immutable: a status transition produces a new record, which keeps the
 * state inside the {@code ConcurrentHashMap} simple and race-free.
 *
 * @param protocolo protocol returned on acceptance, e.g. {@code PRT-20261126-0001}
 * @param beneficiarioId beneficiary that owns the installments
 * @param parcelas identifiers of the installments being ported
 * @param status current status of the operation
 * @param motivoRejeicao reason when {@code status} is {@code REJEITADA}, otherwise {@code null}
 * @param criadaEm instant the operation was accepted
 */
public record Operation(
    String protocolo,
    String beneficiarioId,
    List<String> parcelas,
    OperationStatus status,
    String motivoRejeicao,
    Instant criadaEm) {

  public Operation {
    parcelas = List.copyOf(parcelas);
  }

  /** Returns a copy of this operation with the given status and rejection reason. */
  public Operation withStatus(OperationStatus novoStatus, String motivo) {
    return new Operation(protocolo, beneficiarioId, parcelas, novoStatus, motivo, criadaEm);
  }
}
