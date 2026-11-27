package br.com.consigport.adapters.out.central.acl;

import br.com.consigport.adapters.out.central.dto.CentralErrorResponse;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityRequest;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityResponse;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.PortabilityOperation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Anti-corruption layer of the submission of a portability operation.
 *
 * <p>The Central speaks Portuguese; the domain speaks English. This class is the only
 * place that knows the translation: the installments of the operation become the
 * {@code parcelas} of the Central, the receiving account becomes the
 * {@code contaRecebimento} and the answer of the Central becomes the protocol of the
 * domain.
 */
public final class PortabilityAcl {

  /** Code used when the Central answers with a body that is not the contract. */
  private static final String UNEXPECTED_ANSWER_CODE = "ERRO_CENTRAL";

  private final ObjectMapper mapper;

  /** Builds the layer with its own mapper: the contract of the Central is external. */
  public PortabilityAcl() {
    this.mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Translates the operation into the request of the Central.
   *
   * <p>The signature is left empty: the adapter signs the canonical body of this
   * request and then mirrors the signature in the field {@code assinatura}.
   *
   * @param operation the operation to submit
   * @return the request of the Central
   */
  public CentralPortabilityRequest toCentralRequest(PortabilityOperation operation) {
    if (operation == null) {
      throw new IllegalArgumentException("operation must not be null");
    }
    return new CentralPortabilityRequest(
        operation.beneficiaryId().value(),
        operation.installments().stream().map(snapshot -> snapshot.installmentId().value()).toList(),
        operation.assigneeCreditor(),
        new CentralPortabilityRequest.ContaRecebimento(
            operation.receivingAccount().bank(),
            operation.receivingAccount().branch(),
            operation.receivingAccount().account()),
        null);
  }

  /**
   * Mirrors the signature in the request of the Central.
   *
   * @param request the request without the signature
   * @param signature the signature of the canonical body
   * @return the request with the signature
   */
  public CentralPortabilityRequest withSignature(
      CentralPortabilityRequest request, String signature) {
    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }
    return new CentralPortabilityRequest(
        request.beneficiarioId(),
        request.parcelas(),
        request.credorCessionario(),
        request.contaRecebimento(),
        signature);
  }

  /**
   * Translates the answer of the Central into the protocol of the domain.
   *
   * @param response the answer of the Central
   * @return the protocol
   * @throws IllegalArgumentException when the Central answers without a protocol
   */
  public String toProtocol(CentralPortabilityResponse response) {
    if (response == null || response.protocolo() == null || response.protocolo().isBlank()) {
      throw new IllegalArgumentException("the Central accepted the operation without a protocol");
    }
    return response.protocolo().trim();
  }

  /**
   * Translates the refusal of the Central into the failure of the domain.
   *
   * <p>The code of the Central is kept as it is: it explains what the Central answered
   * in the trail of the audit, without being translated.
   *
   * @param status the HTTP status answered by the Central
   * @param rawBody the raw body of the answer, which may be empty
   * @param fallbackMessage the message used when the Central did not explain itself
   * @return the failure of the domain
   */
  public CentralSubmissionException toSubmissionFailure(
      int status, String rawBody, String fallbackMessage) {
    String code = UNEXPECTED_ANSWER_CODE;
    String message = fallbackMessage;
    if (rawBody != null && !rawBody.isBlank()) {
      try {
        CentralErrorResponse error = mapper.readValue(rawBody, CentralErrorResponse.class);
        if (error.codigo() != null && !error.codigo().isBlank()) {
          code = error.codigo();
        }
        if (error.mensagem() != null && !error.mensagem().isBlank()) {
          message = error.mensagem();
        }
      } catch (JsonProcessingException exception) {
        // The Central answered with a body that is not the contract: the status and the
        // generic code explain the failure.
      }
    }
    return new CentralSubmissionException(status, code, message);
  }
}
