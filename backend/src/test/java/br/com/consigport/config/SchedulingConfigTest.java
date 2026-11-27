package br.com.consigport.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.application.portability.IntegrationBackoff;
import br.com.consigport.application.portability.PollOperation;
import br.com.consigport.domain.audit.InMemoryOperationEventRepository;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.InMemoryCentralPortabilityPort;
import br.com.consigport.domain.portability.InMemoryCentralStatusPort;
import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The worker that follows the operations until the Central answers.
 *
 * <p>The worker wakes up every 2 s and polls a batch of 20 operations
 * ({@code ARCHITECTURE.md} §11): the test drives the method of the scheduler, so the
 * behaviour is proven without waiting.
 */
class SchedulingConfigTest {

  private static final Instant NOW = Instant.parse("2026-11-27T10:15:30Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private InMemoryCentralStatusPort status;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    status = new InMemoryCentralStatusPort();
  }

  @Test
  void pollsTheBatchWhenTheWorkerIsEnabled() {
    operations.save(pending().pollAttempt(NOW.minusSeconds(1)));
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    new SchedulingConfig(worker(), true).pollPortabilityOperations();

    assertThat(status.polls()).isEqualTo(1);
    assertThat(operations.findByProtocol(PROTOCOL).orElseThrow().status())
        .isEqualTo(OperationStatus.CONFIRMED);
  }

  @Test
  void doesNotPollWhenTheWorkerIsDisabled() {
    operations.save(pending().pollAttempt(NOW.minusSeconds(1)));
    status.answerWith(new CentralStatus.Confirmed("{\"status\":\"CONFIRMADA\"}"));

    new SchedulingConfig(worker(), false).pollPortabilityOperations();

    assertThat(status.polls()).isZero();
    assertThat(operations.findByProtocol(PROTOCOL).orElseThrow().status())
        .isEqualTo(OperationStatus.PENDING);
  }

  private PollOperation worker() {
    return new PollOperation(
        operations,
        new InMemoryCentralPortabilityPort(),
        status,
        new InMemoryOperationEventRepository(),
        work -> work.run(),
        Clock.fixed(NOW, ZoneOffset.UTC),
        new IntegrationBackoff(Duration.ofSeconds(2), Duration.ofSeconds(60)),
        20);
  }

  private static PortabilityOperation pending() {
    return PortabilityOperation.create(
            new PortabilityOperation.OperationId(UUID.randomUUID()),
            new IdempotencyKey(UUID.randomUUID()),
            new BeneficiaryId("BEN-001"),
            new AgreementId("AGR-001"),
            List.of(
                new InstallmentSnapshot(
                    new InstallmentId("PAR-001"),
                    new AgreementId("AGR-001"),
                    new Money(new BigDecimal("450.00")),
                    LocalDate.parse("2026-12-05"))),
            "ConsigPort",
            new ReceivingAccount("001", "1234", "56789-0"),
            NOW)
        .submit(PROTOCOL);
  }
}
