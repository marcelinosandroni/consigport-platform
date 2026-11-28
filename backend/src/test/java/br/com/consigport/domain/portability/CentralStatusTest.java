package br.com.consigport.domain.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the answers of the Central and of the failure of the submission.
 *
 * <p>The anti-corruption layer translates the four shapes of the Central into the sealed
 * hierarchy of {@link CentralStatus}: the domain refuses an answer it cannot read, and the
 * failure of the submission carries the stable code of the Central, so the trail of the
 * audit explains what the Central answered without translating it.
 */
class CentralStatusTest {

  @Nested
  class PendingTest {

    @Test
    void carriesTheWaitAndTheRawPayload() {
      CentralStatus.Pending pending = new CentralStatus.Pending(Duration.ofSeconds(2), "{}");

      assertThat(pending.retryAfter()).isEqualTo(Duration.ofSeconds(2));
      assertThat(pending.rawPayload()).isEqualTo("{}");
    }

    @Test
    void refusesAMissingWait() {
      assertThatThrownBy(() -> new CentralStatus.Pending(null, "{}"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("retryAfter must not be negative");
    }

    @Test
    void refusesANegativeWait() {
      assertThatThrownBy(() -> new CentralStatus.Pending(Duration.ofSeconds(-1), "{}"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("retryAfter must not be negative");
    }
  }

  @Nested
  class RejectedTest {

    @Test
    void carriesTheReasonAndTheRawPayload() {
      CentralStatus.Rejected rejected =
          new CentralStatus.Rejected(new RejectionReason.MarginInsufficient(), "{}");

      assertThat(rejected.reason())
          .isInstanceOf(RejectionReason.MarginInsufficient.class);
      assertThat(rejected.rawPayload()).isEqualTo("{}");
    }

    @Test
    void refusesAMissingReason() {
      assertThatThrownBy(() -> new CentralStatus.Rejected(null, "{}"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("reason must not be null");
    }
  }

  @Nested
  class NotFoundTest {

    @Test
    void carriesTheUnknownProtocolAndTheRawPayload() {
      CentralStatus.NotFound notFound = new CentralStatus.NotFound("PRT-20261127-0001", "{}");

      assertThat(notFound.protocol()).isEqualTo("PRT-20261127-0001");
      assertThat(notFound.rawPayload()).isEqualTo("{}");
    }

    @Test
    void refusesAMissingProtocol() {
      assertThatThrownBy(() -> new CentralStatus.NotFound(null, "{}"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("protocol must not be blank");
    }

    @Test
    void refusesABlankProtocol() {
      assertThatThrownBy(() -> new CentralStatus.NotFound("  ", "{}"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("protocol must not be blank");
    }
  }

  @Nested
  class ConfirmedTest {

    @Test
    void carriesTheRawPayload() {
      assertThat(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}").rawPayload())
          .isEqualTo("{\"status\":\"CONFIRMADA\"}");
    }
  }

  @Nested
  class SubmissionFailureTest {

    @Test
    void carriesTheStatusAndTheCodeOfTheCentral() {
      CentralSubmissionException failure =
          new CentralSubmissionException(422, "PARCELA_INVALIDA", "the installment is closed");

      assertThat(failure.status()).isEqualTo(422);
      assertThat(failure.code()).isEqualTo("PARCELA_INVALIDA");
      assertThat(failure).hasMessage("the installment is closed");
    }

    @Test
    void namesTheUnreachableCentralWhenThereIsNoAnswer() {
      CentralSubmissionException failure = new CentralSubmissionException("no answer");

      assertThat(failure.status()).isZero();
      assertThat(failure.code()).isEqualTo(CentralSubmissionException.UNREACHABLE_CODE);
    }

    @Test
    void namesTheUnreachableCentralWhenTheCodeIsAbsent() {
      assertThat(new CentralSubmissionException(500, null, "failed").code())
          .isEqualTo(CentralSubmissionException.UNREACHABLE_CODE);
    }

    @Test
    void namesTheUnreachableCentralWhenTheCodeIsBlank() {
      assertThat(new CentralSubmissionException(500, "  ", "failed").code())
          .isEqualTo(CentralSubmissionException.UNREACHABLE_CODE);
    }
  }

  @Nested
  class FilterTest {

    @Test
    void refusesAMissingStatus() {
      assertThatThrownBy(() -> new OperationFilter.WithStatus(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("status must not be null");
    }
  }
}
