package br.com.consigport.domain.portability;

import java.time.Duration;

/**
 * Situation of a portability operation in the Central, as the domain understands it.
 *
 * <p>The Central answers the status of an operation with four shapes: {@code 204} while
 * the processing continues, {@code 200 CONFIRMADA} when it ends well, {@code 200
 * REJEITADA} when it ends with a reason and {@code 404} when it does not know the
 * protocol. The anti-corruption layer translates the Portuguese contract into this
 * sealed hierarchy, so the application never reads a status of the Central.
 *
 * <p>Every answer carries the raw body of the Central, when there is one, so the trail of
 * the audit proves what the Central answered (ADR {@code 0003}). The body is opaque text:
 * the domain never parses it.
 */
public sealed interface CentralStatus {

  /**
   * The Central is still processing the operation.
   *
   * @param retryAfter how long the caller should wait before asking again; it comes from
   *     the {@code Retry-After} header of the Central
   * @param rawPayload the raw body of the Central, {@code null} when the answer had none
   */
  record Pending(Duration retryAfter, String rawPayload) implements CentralStatus {

    /**
     * Validates the answer.
     *
     * @param retryAfter how long to wait before the next poll
     * @param rawPayload the raw body of the Central
     */
    public Pending {
      if (retryAfter == null || retryAfter.isNegative()) {
        throw new IllegalArgumentException("retryAfter must not be negative");
      }
    }
  }

  /**
   * The Central confirmed the operation.
   *
   * @param rawPayload the raw body of the Central
   */
  record Confirmed(String rawPayload) implements CentralStatus {}

  /**
   * The Central rejected the operation.
   *
   * @param reason why the Central rejected it
   * @param rawPayload the raw body of the Central
   */
  record Rejected(RejectionReason reason, String rawPayload) implements CentralStatus {

    /**
     * Validates the answer.
     *
     * @param reason why the Central rejected the operation
     * @param rawPayload the raw body of the Central
     */
    public Rejected {
      if (reason == null) {
        throw new IllegalArgumentException("reason must not be null");
      }
    }
  }

  /**
   * The Central does not know the protocol of the operation.
   *
   * <p>The Central answered normally, so it is available: it simply does not know the
   * protocol. The operation stays resumable and the worker tries again with the backoff,
   * because the protocol may exist again (the Central may have lost it and recovered it).
   *
   * @param protocol the protocol the Central does not know
   * @param rawPayload the raw body of the answer of the Central
   */
  record NotFound(String protocol, String rawPayload) implements CentralStatus {

    /**
     * Validates the answer.
     *
     * @param protocol the protocol the Central does not know
     * @param rawPayload the raw body of the answer of the Central
     */
    public NotFound {
      if (protocol == null || protocol.isBlank()) {
        throw new IllegalArgumentException("protocol must not be blank");
      }
    }
  }
}
