package br.com.consigport.central.domain;

import java.time.Instant;

/**
 * Beneficiary opt-in that unlocks the agenda of an agreement.
 *
 * @param autorizacaoId identifier returned to the caller, e.g. {@code AUT-20261126-0001}
 * @param beneficiarioId beneficiary that granted the consent
 * @param convenioId agreement covered by the consent
 * @param criadaEm instant the authorization was recorded
 */
public record Authorization(
    String autorizacaoId, String beneficiarioId, String convenioId, Instant criadaEm) {}
