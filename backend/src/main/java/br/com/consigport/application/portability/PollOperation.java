package br.com.consigport.application.portability;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.CentralPortabilityPort;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.CentralStatusPort;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.ConcurrentOperationUpdateException;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.portability.RejectionReason;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.TransactionRunner;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Follows the operations until the Central answers with a terminal situation (ADR
 * {@code 0003}).
 *
 * <p>The worker reads the operations whose polling is due, in a batch, and asks the
 * Central about each one. An operation without a protocol is sent again with the same
 * idempotency key, never as a new operation. An operation with a protocol is polled:
 * {@code 204} records a {@code POLL_ATTEMPT} and schedules the next poll with the
 * {@code Retry-After} of the Central, a terminal answer records the new status, and a
 * failure of the Central records an {@code INTEGRATION_FAILED} and schedules the retry
 * with the backoff.
 *
 * <p>Every transition is written with its event in one transaction (BR-12): the trail of
 * the audit proves what happened with the operation.
 */
public final class PollOperation {

  private final PortabilityOperationRepository operations;
  private final CentralPortabilityPort central;
  private final CentralStatusPort status;
  private final OperationEventRepository events;
  private final TransactionRunner transactions;
  private final Clock clock;
  private final IntegrationBackoff backoff;
  private final int batchSize;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   * @param central the submission of the operation to the Central
   * @param status the read of the situation of the operation in the Central
   * @param events the append-only trail of the audit
   * @param transactions the boundary of the transaction of each transition
   * @param clock the clock that dates the polls
   * @param backoff the wait between two attempts after a failure
   * @param batchSize how many operations a single batch polls
   */
  public PollOperation(
      PortabilityOperationRepository operations,
      CentralPortabilityPort central,
      CentralStatusPort status,
      OperationEventRepository events,
      TransactionRunner transactions,
      Clock clock,
      IntegrationBackoff backoff,
      int batchSize) {
    this.operations = operations;
    this.central = central;
    this.status = status;
    this.events = events;
    this.transactions = transactions;
    this.clock = clock;
    this.backoff = backoff;
    this.batchSize = batchSize;
  }

  /**
   * Polls the batch of operations that is due.
   *
   * <p>The instant of the query dates the batch: an operation that becomes due while the
   * batch is running waits for the next one.
   *
   * @return how many operations were polled
   */
  public PollResult pollBatch() {
    Instant now = clock.instant();
    List<PortabilityOperation> due = operations.findDueForPolling(now, batchSize);
    for (PortabilityOperation operation : due) {
      poll(operation);
    }
    return new PollResult(due.size());
  }

  /**
   * Polls one operation.
   *
   * <p>The instant of the transition is the one of the answer of the Central, not the
   * one of the beginning of the batch: a poll that took seconds must not schedule the
   * next one in the past.
   *
   * @param operation the operation to poll
   */
  private void poll(PortabilityOperation operation) {
    if (operation.protocol() == null) {
      sendAgain(operation);
      return;
    }
    try {
      CentralStatus answer = status.statusOf(operation.protocol());
      Instant answeredAt = clock.instant();
      OperationStatus previousStatus = operation.status();
      switch (answer) {
        case CentralStatus.Pending pending -> {
          Instant nextPoll = answeredAt.plus(pending.retryAfter());
          PortabilityOperation stillPending = operation.pollAttempt(nextPoll);
          record(
              stillPending,
              pollAttemptEvent(
                  stillPending, previousStatus, answeredAt, pending.retryAfter(), pending.rawPayload()));
        }
        case CentralStatus.Confirmed confirmed -> {
          PortabilityOperation confirmedOperation = operation.confirm();
          record(
              confirmedOperation,
              confirmedEvent(confirmedOperation, previousStatus, answeredAt, confirmed.rawPayload()));
        }
        case CentralStatus.Rejected rejected -> {
          PortabilityOperation rejectedOperation = operation.reject(rejected.reason());
          record(
              rejectedOperation,
              rejectedEvent(
                  rejectedOperation, previousStatus, answeredAt, rejected.reason(), rejected.rawPayload()));
        }
      }
    } catch (DomainError.CentralUnavailable failure) {
      fail(operation, clock.instant(), failure.getMessage());
    }
  }

  /**
   * Sends the operation again, with the same idempotency key.
   *
   * <p>The operation exists and the Central never received it: sending it again does not
   * create a second operation, because the key is the same (BR-07, BR-15).
   *
   * @param operation the operation without a protocol
   */
  private void sendAgain(PortabilityOperation operation) {
    OperationStatus previousStatus = operation.status();
    try {
      String protocol = central.submit(operation);
      Instant answeredAt = clock.instant();
      PortabilityOperation pending = operation.submit(protocol);
      record(pending, submittedEvent(pending, previousStatus, answeredAt));
    } catch (CentralSubmissionException failure) {
      fail(operation, clock.instant(), "the Central did not accept the operation: " + failure.code());
    }
  }

  /**
   * Records the failure of the Central and schedules the next attempt.
   *
   * <p>The operation that was already in {@code INTEGRATION_ERROR} keeps the status: the
   * state machine of {@code ARCHITECTURE.md} §7 has no transition from {@code
   * INTEGRATION_ERROR} to {@code INTEGRATION_ERROR}. What grows is the backoff and the
   * count of the failures.
   *
   * @param operation the operation whose integration failed
   * @param now the instant of the failure
   * @param reason what the Central answered
   */
  private void fail(PortabilityOperation operation, Instant now, String reason) {
    OperationStatus previousStatus = operation.status();
    int failures = operation.retryCount() + 1;
    Instant nextPoll = now.plus(backoff.delayFor(failures));
    PortabilityOperation failed =
        previousStatus == OperationStatus.INTEGRATION_ERROR
            ? operation.renewIntegrationBackoff(nextPoll)
            : operation.registerIntegrationFailure(nextPoll);
    record(failed, integrationFailedEvent(failed, previousStatus, now, reason));
  }

  /**
   * Writes the operation and the event of the transition in one transaction.
   *
   * <p>When another worker changed the operation in the meantime, the stored operation is
   * the truth and this attempt is discarded: the optimistic lock of the storage decides
   * who moves the operation forward.
   *
   * @param operation the operation with the new state
   * @param event the event of the transition
   */
  private void record(PortabilityOperation operation, OperationEvent event) {
    try {
      transactions.run(
          () -> {
            operations.save(operation);
            events.append(event);
          });
    } catch (ConcurrentOperationUpdateException conflict) {
      // Another worker moved the operation forward: the stored state is the truth.
    }
  }

  private static OperationEvent pollAttemptEvent(
      PortabilityOperation operation,
      OperationStatus previousStatus,
      Instant now,
      Duration retryAfter,
      String rawPayload) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        operation.status(),
        EventType.POLL_ATTEMPT,
        "The Central is still processing the operation; the next poll is in "
            + retryAfter.toSeconds()
            + " s",
        rawPayload,
        now);
  }

  private static OperationEvent confirmedEvent(
      PortabilityOperation operation,
      OperationStatus previousStatus,
      Instant now,
      String rawPayload) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        OperationStatus.CONFIRMED,
        EventType.OPERATION_CONFIRMED,
        "The Central confirmed the operation",
        rawPayload,
        now);
  }

  private static OperationEvent rejectedEvent(
      PortabilityOperation operation,
      OperationStatus previousStatus,
      Instant now,
      RejectionReason reason,
      String rawPayload) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        OperationStatus.REJECTED,
        EventType.OPERATION_REJECTED,
        "The Central rejected the operation: " + reason.code(),
        rawPayload,
        now);
  }

  private static OperationEvent submittedEvent(
      PortabilityOperation operation, OperationStatus previousStatus, Instant now) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        operation.status(),
        EventType.OPERATION_SUBMITTED,
        "Central accepted the operation with protocol " + operation.protocol(),
        null,
        now);
  }

  private static OperationEvent integrationFailedEvent(
      PortabilityOperation operation,
      OperationStatus previousStatus,
      Instant now,
      String reason) {
    return OperationEvent.record(
        operation.id(),
        previousStatus,
        OperationStatus.INTEGRATION_ERROR,
        EventType.INTEGRATION_FAILED,
        reason,
        null,
        now);
  }

  /**
   * The outcome of one batch of polls.
   *
   * @param polled how many operations were polled
   */
  public record PollResult(int polled) {}
}
