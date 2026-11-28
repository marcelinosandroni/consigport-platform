package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationDetail;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the consultation of one portability operation.
 *
 * <p>The operation is the aggregate of the whole history: the snapshot of the
 * installments, the protocol, the status and the instant of the update of the account.
 */
class FindOperationTest {

  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private FindOperation useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    useCase = new FindOperation(operations);
  }

  @Test
  void findsTheOperationWithItsWholeHistory() {
    PortabilityOperation completed =
        operation().submit(PROTOCOL).confirm().complete(NOW.plusSeconds(10));
    operations.save(completed);

    OperationDetail found = useCase.execute(completed.id());

    assertThat(found.status()).isEqualTo(OperationStatus.COMPLETED);
    assertThat(found.protocol()).isEqualTo(PROTOCOL);
    assertThat(found.accountUpdatedAt()).isEqualTo(NOW.plusSeconds(10));
    assertThat(found.installments()).hasSize(1);
  }

  @Test
  void refusesAnOperationThatDoesNotExist() {
    PortabilityOperation.OperationId unknown =
        new PortabilityOperation.OperationId(UUID.randomUUID());

    assertThatThrownBy(() -> useCase.execute(unknown))
        .isInstanceOf(DomainError.OperationNotFound.class)
        .hasMessage("operation not found: " + unknown.value())
        .extracting(exception -> ((DomainError.OperationNotFound) exception).code())
        .isEqualTo("OPERATION_NOT_FOUND");
  }

  private static PortabilityOperation operation() {
    return PortabilityOperation.create(
        new PortabilityOperation.OperationId(
            UUID.nameUUIDFromBytes("operation-2.14".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
        new IdempotencyKey(
            UUID.nameUUIDFromBytes("key-2.14".getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
        NOW);
  }
}
