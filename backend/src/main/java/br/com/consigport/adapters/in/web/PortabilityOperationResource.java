package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.OperationAcceptedResponse;
import br.com.consigport.adapters.in.web.dto.PortabilityOperationRequest;
import br.com.consigport.application.portability.StartOperation;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public edge of the start of a portability operation.
 *
 * <p>The answer is {@code 202}: the confirmation of the Central is asynchronous. The
 * header {@code Idempotent-Replay} tells the caller that the operation already existed
 * for the key that was sent.
 */
@RestController
@RequestMapping("/api/v1/portability-operations")
public class PortabilityOperationResource {

  /** The header that carries the key of the idempotency. */
  public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

  /** The header that tells the caller that the operation already existed. */
  public static final String IDEMPOTENT_REPLAY_HEADER = "Idempotent-Replay";

  private final StartOperation startOperation;

  /**
   * Builds the resource.
   *
   * @param startOperation the use case that starts the operation
   */
  public PortabilityOperationResource(StartOperation startOperation) {
    this.startOperation = startOperation;
  }

  /**
   * Starts the operation.
   *
   * @param idempotencyKey the raw key of the idempotency
   * @param request the request of the operation
   * @return the accepted operation
   * @throws DomainError.IdempotencyKeyRequired when the key is missing
   * @throws DomainError.IdempotencyConflict when the key was used with another payload
   * @throws DomainError.InstallmentNotEligible when an installment is not eligible
   */
  @PostMapping
  public ResponseEntity<OperationAcceptedResponse> start(
      @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
      @Valid @RequestBody PortabilityOperationRequest request) {
    StartOperation.StartOperationResult result =
        startOperation.execute(toDomainRequest(request), toIdempotencyKey(idempotencyKey));

    return ResponseEntity.accepted()
        .header(IDEMPOTENT_REPLAY_HEADER, Boolean.toString(result.replay()))
        .body(toResponse(result.operation()));
  }

  private static IdempotencyKey toIdempotencyKey(String rawKey) {
    if (rawKey == null || rawKey.isBlank()) {
      throw new DomainError.IdempotencyKeyRequired();
    }
    return IdempotencyKey.parse(rawKey);
  }

  private static OperationRequest toDomainRequest(PortabilityOperationRequest request) {
    return new OperationRequest(
        new BeneficiaryId(request.beneficiaryId()),
        new AgreementId(request.agreementId()),
        request.installmentIds().stream().map(InstallmentId::new).toList(),
        request.assigneeCreditor(),
        new ReceivingAccount(
            request.receivingAccount().bank(),
            request.receivingAccount().branch(),
            request.receivingAccount().account()));
  }

  private static OperationAcceptedResponse toResponse(PortabilityOperation operation) {
    return new OperationAcceptedResponse(
        operation.id().value().toString(),
        operation.protocol(),
        operation.status(),
        operation.totalAmount().amount(),
        operation.createdAt());
  }
}
