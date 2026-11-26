package br.com.consigport.application.portability;

import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.TransactionRunner;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Starts a portability operation (BR-04, BR-05, BR-07, BR-12).
 *
 * <p>The use case is idempotent: the same key with the same payload answers the
 * operation that already exists, and the same key with another payload is a conflict.
 * The eligibility of the installments is read from the agenda of the beneficiary, and
 * the amounts and dates of the moment are frozen in the snapshot of the operation.
 *
 * <p>The operation and its creation event are written in the same transaction, so the
 * trail of the audit never loses an event.
 */
public final class StartOperation {

  private final PortabilityOperationRepository operations;
  private final AgendaGateway agenda;
  private final OperationEventRepository events;
  private final OperationPolicy policy;
  private final TransactionRunner transactions;
  private final Clock clock;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   * @param agenda the read of the agenda in the Central
   * @param events the append-only trail of the audit
   * @param policy the rule of the eligibility of the installments
   * @param transactions the boundary of the transaction of the creation
   * @param clock the clock that dates the creation
   */
  public StartOperation(
      PortabilityOperationRepository operations,
      AgendaGateway agenda,
      OperationEventRepository events,
      OperationPolicy policy,
      TransactionRunner transactions,
      Clock clock) {
    this.operations = operations;
    this.agenda = agenda;
    this.events = events;
    this.policy = policy;
    this.transactions = transactions;
    this.clock = clock;
  }

  /**
   * Starts the operation.
   *
   * @param request the request of the operation
   * @param idempotencyKey the key that makes the request idempotent
   * @return the operation and whether it already existed
   * @throws DomainError.IdempotencyConflict when the key was already used with another
   *     payload
   * @throws DomainError.InstallmentNotEligible when an installment is not eligible
   */
  public StartOperationResult execute(OperationRequest request, IdempotencyKey idempotencyKey) {
    Optional<PortabilityOperation> existing = operations.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      return replay(request, idempotencyKey, existing.get());
    }

    PortabilityOperation operation = create(request, idempotencyKey);
    try {
      transactions.run(
          () -> {
            operations.save(operation);
            events.append(creationEvent(operation));
          });
    } catch (IdempotencyKeyAlreadyUsedException exception) {
      // Another request took the key first: the winner answers this caller too.
      PortabilityOperation winner =
          operations
              .findByIdempotencyKey(idempotencyKey)
              .orElseThrow(() -> exception);
      return replay(request, idempotencyKey, winner);
    }
    return new StartOperationResult(operation, false);
  }

  private StartOperationResult replay(
      OperationRequest request, IdempotencyKey idempotencyKey, PortabilityOperation operation) {
    if (!request.matches(operation)) {
      throw new DomainError.IdempotencyConflict(idempotencyKey);
    }
    return new StartOperationResult(operation, true);
  }

  private PortabilityOperation create(OperationRequest request, IdempotencyKey idempotencyKey) {
    List<InstallmentSnapshot> snapshot =
        policy.freeze(request, agenda.findAgenda(request.beneficiaryId()));
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(UUID.randomUUID()),
        idempotencyKey,
        request.beneficiaryId(),
        request.agreementId(),
        snapshot,
        request.assigneeCreditor(),
        request.receivingAccount(),
        // The storage keeps microseconds: the answer of the creation and the answer of
        // the replay are the same when the instant is truncated to that precision.
        clock.instant().truncatedTo(ChronoUnit.MICROS));
  }

  private static OperationEvent creationEvent(PortabilityOperation operation) {
    return OperationEvent.record(
        operation.id(),
        null,
        operation.status(),
        EventType.OPERATION_CREATED,
        "Operation created",
        null,
        operation.createdAt());
  }

  /**
   * The answer of the start of an operation.
   *
   * @param operation the operation, new or existing
   * @param replay whether the operation already existed for the key
   */
  public record StartOperationResult(PortabilityOperation operation, boolean replay) {}
}
