package br.com.consigport.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Vocabulary of the audit trail.
 *
 * <p>The types are the ones persisted and exposed by the public API: the set is closed.
 */
class EventTypeTest {

  @Test
  void declaresTheStableEventTypes() {
    assertThat(EventType.values())
        .containsExactly(
            EventType.OPERATION_CREATED,
            EventType.OPERATION_SUBMITTED,
            EventType.POLL_ATTEMPT,
            EventType.OPERATION_CONFIRMED,
            EventType.OPERATION_REJECTED,
            EventType.ACCOUNT_UPDATED,
            EventType.OPERATION_COMPLETED,
            EventType.INTEGRATION_FAILED);
  }
}
