package br.com.consigport.application.portability;

import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.PortabilityOperation.InvalidTransitionException;
import br.com.consigport.domain.portability.RejectionReason;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Closes the cycle of a portability operation when the Central answers with a terminal
 * situation (the flow of the worker, {@code ARCHITECTURE.md} §11).
 *
 * <p>A confirmation closes the operation in one transaction: the status becomes
 * {@code CONFIRMED}, the receiving account is updated with the instant of the update and
 * the operation becomes {@code COMPLETED}. The trail proves the whole path — {@code
 * OPERATION_CONFIRMED}, {@code ACCOUNT_UPDATED} and {@code OPERATION_COMPLETED} — and
 * nothing is written when any of the steps fails.
 *
 * <p>A rejection closes the operation as {@code REJECTED} with the reason of the Central,
 * also in one transaction with its event.
 */
public final class CompleteOperation {

  private final TransitionRecorder recorder;
  private final Clock clock;

  /**
   * Builds the use case.
   *
   * @param recorder the writer of the transition and of its trail
   * @param clock the clock that dates the closure
   */
  public CompleteOperation(TransitionRecorder recorder, Clock clock) {
    this.recorder = recorder;
    this.clock = clock;
  }

  /**
   * Closes the operation that the Central confirmed.
   *
   * <p>The operation is confirmed, the receiving account is updated and the operation is
   * completed: the three steps happen in one transaction, so the trail never shows a
   * closure that did not happen.
   *
   * @param operation the operation confirmed by the Central
   * @param rawPayload the raw answer of the Central
   * @throws InvalidTransitionException when the operation cannot be confirmed from its
   *     current status
   */
  public void confirm(PortabilityOperation operation, String rawPayload) {
    OperationStatus previousStatus = operation.status();
    Instant confirmedAt = clock.instant();
    PortabilityOperation confirmed = operation.confirm();
    PortabilityOperation completed = confirmed.complete(confirmedAt);
    recorder.record(
        completed,
        List.of(
            confirmedEvent(confirmed, previousStatus, confirmedAt, rawPayload),
            accountUpdatedEvent(completed, confirmedAt),
            completedEvent(completed, confirmedAt)));
  }

  /**
   * Closes the operation that the Central rejected.
   *
   * @param operation the operation rejected by the Central
   * @param reason the reason of the rejection
   * @param rawPayload the raw answer of the Central
   * @throws InvalidTransitionException when the operation cannot be rejected from its
   *     current status
   */
  public void reject(PortabilityOperation operation, RejectionReason reason, String rawPayload) {
    OperationStatus previousStatus = operation.status();
    Instant rejectedAt = clock.instant();
    PortabilityOperation rejected = operation.reject(reason);
    recorder.record(
        rejected,
        List.of(rejectedEvent(rejected, previousStatus, rejectedAt, reason, rawPayload)));
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

  private static OperationEvent accountUpdatedEvent(PortabilityOperation operation, Instant now) {
    return OperationEvent.record(
        operation.id(),
        OperationStatus.CONFIRMED,
        OperationStatus.COMPLETED,
        EventType.ACCOUNT_UPDATED,
        "The receiving account of the operation was updated",
        null,
        now);
  }

  private static OperationEvent completedEvent(PortabilityOperation operation, Instant now) {
    return OperationEvent.record(
        operation.id(),
        OperationStatus.CONFIRMED,
        OperationStatus.COMPLETED,
        EventType.OPERATION_COMPLETED,
        "The operation was completed",
        null,
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
}
