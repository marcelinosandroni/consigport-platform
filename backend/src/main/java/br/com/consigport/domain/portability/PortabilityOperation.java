package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Root of the portability context: the lifecycle of one requested operation.
 *
 * <p>The aggregate is immutable: every transition returns a new instance, so the state
 * of an operation can never be changed outside the rules of the state machine of
 * {@code ARCHITECTURE.md} §7. The snapshot of the installments, the total, the
 * beneficiary, the agreement, the idempotency key and the creation instant never change;
 * the status, the protocol, the rejection reason, the retry count, the next poll and the
 * account update instant follow the transitions.
 *
 * <p>Absent values are {@code null}: the protocol is unknown while the operation is
 * {@code CREATED}, the rejection reason exists only when the operation is
 * {@code REJECTED}, the next poll and the account update are set only when they
 * happen, and the account update exists only when the operation is {@code COMPLETED}.
 */
public record PortabilityOperation(
    OperationId id,
    IdempotencyKey idempotencyKey,
    BeneficiaryId beneficiaryId,
    AgreementId agreementId,
    List<InstallmentSnapshot> installments,
    String assigneeCreditor,
    ReceivingAccount receivingAccount,
    Money totalAmount,
    OperationStatus status,
    String protocol,
    RejectionReason rejectionReason,
    int retryCount,
    Instant nextPollAt,
    Instant accountUpdatedAt,
    Instant createdAt) {

  /**
   * Validates the aggregate and freezes the snapshot.
   *
   * @param id the identity of the operation
   * @param idempotencyKey the key that makes the request idempotent
   * @param beneficiaryId the beneficiary that owns the installments
   * @param agreementId the agreement the installments belong to
   * @param installments the frozen installments; at least one
   * @param assigneeCreditor the assignee creditor returned by the Central
   * @param receivingAccount the account that receives the portability
   * @param totalAmount the sum of the installments
   * @param status the current status
   * @param protocol the protocol returned by the Central, {@code null} while {@code CREATED}
   * @param rejectionReason the reason of the rejection, only when {@code REJECTED}
   * @param retryCount how many integration attempts already failed
   * @param nextPollAt when the next poll happens
   * @param accountUpdatedAt when the receiving account was updated
   * @param createdAt the instant of the creation
   * @throws IllegalArgumentException when any invariant is broken
   */
  public PortabilityOperation {
    if (id == null) {
      throw new IllegalArgumentException("operationId must not be null");
    }
    if (idempotencyKey == null) {
      throw new IllegalArgumentException("idempotencyKey must not be null");
    }
    if (beneficiaryId == null) {
      throw new IllegalArgumentException("beneficiaryId must not be null");
    }
    if (agreementId == null) {
      throw new IllegalArgumentException("agreementId must not be null");
    }
    if (installments == null || installments.isEmpty()) {
      throw new IllegalArgumentException("installments must have at least one element");
    }
    installments = List.copyOf(installments);
    if (assigneeCreditor == null || assigneeCreditor.isBlank()) {
      throw new IllegalArgumentException("assigneeCreditor must not be blank");
    }
    assigneeCreditor = assigneeCreditor.trim();
    if (receivingAccount == null) {
      throw new IllegalArgumentException("receivingAccount must not be null");
    }
    if (totalAmount == null) {
      throw new IllegalArgumentException("totalAmount must not be null");
    }
    if (!totalAmount.equals(sumOf(installments))) {
      throw new IllegalArgumentException("totalAmount must be the sum of the installments");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (status == OperationStatus.CREATED && protocol != null) {
      throw new IllegalArgumentException("protocol must be null while the operation is CREATED");
    }
    if (status == OperationStatus.REJECTED && rejectionReason == null) {
      throw new IllegalArgumentException("rejectionReason must not be null when rejected");
    }
    if (status != OperationStatus.REJECTED && rejectionReason != null) {
      throw new IllegalArgumentException("rejectionReason must be null unless rejected");
    }
    if (status == OperationStatus.COMPLETED && accountUpdatedAt == null) {
      throw new IllegalArgumentException("accountUpdatedAt must not be null when completed");
    }
    if (status != OperationStatus.COMPLETED && accountUpdatedAt != null) {
      throw new IllegalArgumentException("accountUpdatedAt must be null unless completed");
    }
    if (retryCount < 0) {
      throw new IllegalArgumentException("retryCount must not be negative");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt must not be null");
    }
  }

  /**
   * Creates a new operation in {@code CREATED}, with the total equal to the sum of the
   * installments.
   *
   * @param id the identity of the operation
   * @param idempotencyKey the key that makes the request idempotent
   * @param beneficiaryId the beneficiary that owns the installments
   * @param agreementId the agreement the installments belong to
   * @param installments the frozen installments; at least one
   * @param assigneeCreditor the assignee creditor returned by the Central
   * @param receivingAccount the account that receives the portability
   * @param createdAt the instant of the creation
   * @return the new operation
   */
  public static PortabilityOperation create(
      OperationId id,
      IdempotencyKey idempotencyKey,
      BeneficiaryId beneficiaryId,
      AgreementId agreementId,
      List<InstallmentSnapshot> installments,
      String assigneeCreditor,
      ReceivingAccount receivingAccount,
      Instant createdAt) {
    return new PortabilityOperation(
        id,
        idempotencyKey,
        beneficiaryId,
        agreementId,
        installments,
        assigneeCreditor,
        receivingAccount,
        sumOf(installments),
        OperationStatus.CREATED,
        null,
        null,
        0,
        null,
        null,
        createdAt);
  }

  /**
   * The Central accepted the operation ({@code 202}).
   *
   * @param protocol the protocol returned by the Central
   * @return the operation in {@code PENDING}
   * @throws InvalidTransitionException when the operation cannot be submitted from its
   *     current status
   */
  public PortabilityOperation submit(String protocol) {
    requireSource(OperationStatus.PENDING, OperationStatus.CREATED, OperationStatus.INTEGRATION_ERROR);
    if (protocol == null || protocol.isBlank()) {
      throw new IllegalArgumentException("protocol must not be blank");
    }
    return transition(
        OperationStatus.PENDING, protocol.trim(), rejectionReason, retryCount, nextPollAt, accountUpdatedAt);
  }

  /**
   * The integration failed after the retries.
   *
   * <p>The operation may already be in {@code INTEGRATION_ERROR}: the state machine of
   * {@code ARCHITECTURE.md} §7 declares the self-transition {@code INTEGRATION_ERROR} to
   * {@code INTEGRATION_ERROR}, with the {@code INTEGRATION_FAILED} event. The status does
   * not change in that case; what changes is the count of the failures and the schedule
   * of the next attempt, which grow with the backoff (the flow of the worker, {@code
   * ARCHITECTURE.md} §11).
   *
   * @param nextPollAt when the next attempt happens
   * @return the operation in {@code INTEGRATION_ERROR}
   * @throws InvalidTransitionException when the operation cannot fail from its current
   *     status
   */
  public PortabilityOperation registerIntegrationFailure(Instant nextPollAt) {
    requireSource(
        OperationStatus.INTEGRATION_ERROR,
        OperationStatus.CREATED,
        OperationStatus.PENDING,
        OperationStatus.INTEGRATION_ERROR);
    if (nextPollAt == null) {
      throw new IllegalArgumentException("nextPollAt must not be null");
    }
    return transition(
        OperationStatus.INTEGRATION_ERROR, protocol, rejectionReason, retryCount + 1, nextPollAt, accountUpdatedAt);
  }

  /**
   * The poll returned {@code 204}: the operation is still pending.
   *
   * @param nextPollAt when the next poll happens
   * @return the operation in {@code PENDING}
   * @throws InvalidTransitionException when the operation cannot be polled from its
   *     current status
   */
  public PortabilityOperation pollAttempt(Instant nextPollAt) {
    requireSource(
        OperationStatus.PENDING, OperationStatus.PENDING, OperationStatus.INTEGRATION_ERROR);
    if (nextPollAt == null) {
      throw new IllegalArgumentException("nextPollAt must not be null");
    }
    return transition(
        OperationStatus.PENDING, protocol, rejectionReason, retryCount, nextPollAt, accountUpdatedAt);
  }

  /**
   * The Central confirmed the operation.
   *
   * @return the operation in {@code CONFIRMED}
   * @throws InvalidTransitionException when the operation cannot be confirmed from its
   *     current status
   */
  public PortabilityOperation confirm() {
    requireSource(
        OperationStatus.CONFIRMED, OperationStatus.PENDING, OperationStatus.INTEGRATION_ERROR);
    return transition(
        OperationStatus.CONFIRMED, protocol, null, retryCount, nextPollAt, accountUpdatedAt);
  }

  /**
   * The Central rejected the operation.
   *
   * @param reason the reason of the rejection
   * @return the operation in {@code REJECTED}
   * @throws InvalidTransitionException when the operation cannot be rejected from its
   *     current status
   */
  public PortabilityOperation reject(RejectionReason reason) {
    requireSource(
        OperationStatus.REJECTED, OperationStatus.PENDING, OperationStatus.INTEGRATION_ERROR);
    if (reason == null) {
      throw new IllegalArgumentException("rejectionReason must not be null");
    }
    return transition(
        OperationStatus.REJECTED, protocol, reason, retryCount, nextPollAt, accountUpdatedAt);
  }

  /**
   * The receiving account was updated: the operation is closed.
   *
   * @param accountUpdatedAt when the account was updated
   * @return the operation in {@code COMPLETED}
   * @throws InvalidTransitionException when the operation cannot be completed from its
   *     current status
   */
  public PortabilityOperation complete(Instant accountUpdatedAt) {
    requireSource(OperationStatus.COMPLETED, OperationStatus.CONFIRMED);
    if (accountUpdatedAt == null) {
      throw new IllegalArgumentException("accountUpdatedAt must not be null");
    }
    return transition(
        OperationStatus.COMPLETED, protocol, null, retryCount, nextPollAt, accountUpdatedAt);
  }

  private void requireSource(OperationStatus target, OperationStatus... allowedSources) {
    if (status.isTerminal()) {
      throw new InvalidTransitionException(status, target);
    }
    for (OperationStatus source : allowedSources) {
      if (status == source) {
        return;
      }
    }
    throw new InvalidTransitionException(status, target);
  }

  private PortabilityOperation transition(
      OperationStatus newStatus,
      String newProtocol,
      RejectionReason newRejectionReason,
      int newRetryCount,
      Instant newNextPollAt,
      Instant newAccountUpdatedAt) {
    return new PortabilityOperation(
        id,
        idempotencyKey,
        beneficiaryId,
        agreementId,
        installments,
        assigneeCreditor,
        receivingAccount,
        totalAmount,
        newStatus,
        newProtocol,
        newRejectionReason,
        newRetryCount,
        newNextPollAt,
        newAccountUpdatedAt,
        createdAt);
  }

  private static Money sumOf(List<InstallmentSnapshot> installments) {
    if (installments == null || installments.isEmpty()) {
      throw new IllegalArgumentException("installments must have at least one element");
    }
    BigDecimal total =
        installments.stream()
            .map(snapshot -> snapshot.amount().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new Money(total);
  }

  /**
   * Identity of a portability operation.
   *
   * @param value the UUID of the operation
   */
  public record OperationId(UUID value) {

    /**
     * Validates the identity.
     *
     * @param value the UUID of the operation
     * @throws IllegalArgumentException when the UUID is {@code null}
     */
    public OperationId {
      if (value == null) {
        throw new IllegalArgumentException("operationId must not be null");
      }
    }

    /**
     * Parses the identity from its textual form.
     *
     * @param value the raw UUID
     * @return the parsed identity
     * @throws IllegalArgumentException when the text is blank or is not a UUID
     */
    public static OperationId parse(String value) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException("operationId must not be blank");
      }
      try {
        return new OperationId(UUID.fromString(value.trim()));
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("operationId must be a UUID", exception);
      }
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }

  /**
   * Raised when a transition is not part of the state machine of
   * {@code ARCHITECTURE.md} §7.
   */
  public static final class InvalidTransitionException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final transient OperationStatus from;
    private final transient OperationStatus to;

    /**
     * Builds the exception for the refused transition.
     *
     * @param from the current status
     * @param to the refused target status
     */
    public InvalidTransitionException(OperationStatus from, OperationStatus to) {
      super("operation cannot move from " + from + " to " + to);
      this.from = from;
      this.to = to;
    }

    /**
     * The status the operation was in.
     *
     * @return the current status
     */
    public OperationStatus from() {
      return from;
    }

    /**
     * The status the operation was not allowed to reach.
     *
     * @return the refused target status
     */
    public OperationStatus to() {
      return to;
    }
  }
}
