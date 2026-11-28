package br.com.consigport.application.portability;

import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.CentralPortabilityPort;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.IdempotencyKeyAlreadyUsedException;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.TransactionRunner;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Starts a portability operation (BR-04, BR-05, BR-07, BR-08, BR-12).
 *
 * <p>The use case is idempotent: the same key with the same payload answers the
 * operation that already exists, and the same key with another payload is a conflict.
 * The eligibility of the installments is read from the agenda of the beneficiary, and
 * the amounts and dates of the moment are frozen in the snapshot of the operation.
 *
 * <p>The operation is created and sent to the Central in two steps. The creation and
 * its event are written in one transaction; the submission happens outside of it,
 * because it is a call to another system; the protocol and the event of the acceptance
 * are written in another transaction. An operation that was created but never submitted
 * is completed by the next request with the same key, and the worker resumes it
 * otherwise (BR-11, BR-15).
 *
 * <p>The Central answers {@code 202} with the protocol and confirms later (BR-08): the
 * operation moves from {@code CREATED} to {@code PENDING}. A submission that fails after
 * the attempts leaves the operation in {@code INTEGRATION_ERROR}, resumable and never
 * duplicated.
 */
public final class StartOperation {

  private final PortabilityOperationRepository operations;
  private final AgendaGateway agenda;
  private final CentralPortabilityPort central;
  private final OperationEventRepository events;
  private final OperationPolicy policy;
  private final TransactionRunner transactions;
  private final Clock clock;
  private final Duration integrationBackoff;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   * @param agenda the read of the agenda in the Central
   * @param central the submission of the operation to the Central
   * @param events the append-only trail of the audit
   * @param policy the rule of the eligibility of the installments
   * @param transactions the boundary of the transaction of the creation
   * @param clock the clock that dates the creation
   * @param integrationBackoff how long the operation waits before the next attempt
   */
  public StartOperation(
      PortabilityOperationRepository operations,
      AgendaGateway agenda,
      CentralPortabilityPort central,
      OperationEventRepository events,
      OperationPolicy policy,
      TransactionRunner transactions,
      Clock clock,
      Duration integrationBackoff) {
    this.operations = operations;
    this.agenda = agenda;
    this.central = central;
    this.events = events;
    this.policy = policy;
    this.transactions = transactions;
    this.clock = clock;
    this.integrationBackoff = integrationBackoff;
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
    return submit(operation, false);
  }

  private StartOperationResult replay(
      OperationRequest request, IdempotencyKey idempotencyKey, PortabilityOperation operation) {
    if (!request.matches(operation)) {
      throw new DomainError.IdempotencyConflict(idempotencyKey);
    }
    if (operation.status() == OperationStatus.CREATED) {
      // The operation was created but never sent: the submission is completed now, with
      // the same idempotency key, so the answer is the same for every caller.
      return submit(operation, true);
    }
    return new StartOperationResult(operation, true);
  }

  private StartOperationResult submit(PortabilityOperation operation, boolean replay) {
    OperationStatus previousStatus = operation.status();
    try {
      String protocol = central.submit(operation);
      PortabilityOperation pending = operation.submit(protocol);
      return new StartOperationResult(
          record(pending, submittedEvent(pending, previousStatus, clock.instant())), replay);
    } catch (CentralSubmissionException exception) {
      PortabilityOperation failed =
          operation.registerIntegrationFailure(clock.instant().plus(integrationBackoff));
      return new StartOperationResult(
          record(
              failed,
              integrationFailedEvent(failed, previousStatus, exception, clock.instant())),
          replay);
    }
  }

  /**
   * Writes the operation and the event of the transition in one transaction.
   *
   * <p>When another transaction changed the operation in the meantime, the stored
   * operation is the truth: it is read and answered, because the caller asked for the
   * operation of the key, not for a particular version of it (BR-07).
   *
   * @param operation the operation with the new state
   * @param event the event of the transition
   * @return the operation that is stored
   */
  private PortabilityOperation record(PortabilityOperation operation, OperationEvent event) {
    try {
      transactions.run(
          () -> {
            operations.save(operation);
            events.append(event);
          });
      return operation;
    } catch (ConcurrentOperationUpdateException conflict) {
      return operations.findById(operation.id()).orElseThrow(() -> conflict);
    }
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

  private static OperationEvent submittedEvent(
      PortabilityOperation operation, OperationStatus previousStatus, Instant instant) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        operation.status(),
        EventType.OPERATION_SUBMITTED,
        "Central accepted the operation with protocol " + operation.protocol(),
        null,
        instant);
  }

  private static OperationEvent integrationFailedEvent(
      PortabilityOperation operation,
      OperationStatus previousStatus,
      CentralSubmissionException failure,
      Instant instant) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        operation.status(),
        EventType.INTEGRATION_FAILED,
        "Central did not accept the operation: "
            + failure.code()
            + " (status "
            + failure.status()
            + ")",
        null,
        instant);
  }

  /**
   * The answer of the start of an operation.
   *
   * @param operation the operation, new or existing
   * @param replay whether the operation already existed for the key
   */
  public record StartOperationResult(PortabilityOperation operation, boolean replay) {}
}
