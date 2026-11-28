package br.com.consigport.application.portability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.consigport.domain.portability.InMemoryPortabilityOperationRepository;
import br.com.consigport.domain.portability.InstallmentSnapshot;
import br.com.consigport.domain.portability.Money;
import br.com.consigport.domain.portability.OperationFilter;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.OperationSummary;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.portability.ReceivingAccount;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.Page;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the paginated consultation of the portability operations.
 *
 * <p>The consultation filters by status and walks the result set one page at a time, from
 * the oldest operation to the newest.
 */
class ListOperationsTest {

  private static final Instant NOW = Instant.parse("2026-11-27T12:00:00Z");
  private static final String PROTOCOL = "PRT-20261127-0001";

  private InMemoryPortabilityOperationRepository operations;
  private ListOperations useCase;

  @BeforeEach
  void setUp() {
    operations = new InMemoryPortabilityOperationRepository();
    useCase = new ListOperations(operations);
  }

  @Test
  void listsTheOperationsFromTheOldestToTheNewest() {
    operations.save(operation(0, OperationStatus.PENDING));
    operations.save(operation(1, OperationStatus.COMPLETED));

    Page<OperationSummary> page = useCase.execute(new OperationFilter.All(), 0, 20);

    assertThat(page.items()).hasSize(2);
    assertThat(page.items().get(0).createdAt()).isEqualTo(NOW);
    assertThat(page.items().get(1).createdAt()).isEqualTo(NOW.plusSeconds(1));
    assertThat(page.totalElements()).isEqualTo(2);
    assertThat(page.totalPages()).isEqualTo(1);
  }

  @Test
  void filtersByStatus() {
    operations.save(operation(0, OperationStatus.PENDING));
    operations.save(operation(1, OperationStatus.COMPLETED));
    operations.save(operation(2, OperationStatus.PENDING));

    Page<OperationSummary> page =
        useCase.execute(new OperationFilter.WithStatus(OperationStatus.PENDING), 0, 20);

    assertThat(page.items()).hasSize(2);
    assertThat(page.items()).allMatch(operation -> operation.status() == OperationStatus.PENDING);
    assertThat(page.totalElements()).isEqualTo(2);
  }

  @Test
  void walksTheResultSetOnePageAtATime() {
    for (int index = 0; index < 25; index++) {
      operations.save(operation(index, OperationStatus.PENDING));
    }

    Page<OperationSummary> first = useCase.execute(new OperationFilter.All(), 0, 10);
    Page<OperationSummary> second = useCase.execute(new OperationFilter.All(), 1, 10);
    Page<OperationSummary> third = useCase.execute(new OperationFilter.All(), 2, 10);

    assertThat(first.items()).hasSize(10);
    assertThat(second.items()).hasSize(10);
    assertThat(third.items()).hasSize(5);
    assertThat(first.totalElements()).isEqualTo(25);
    assertThat(first.totalPages()).isEqualTo(3);
    assertThat(third.isLast()).isTrue();
  }

  @Test
  void refusesAPageBeforeTheFirstOne() {
    assertThatThrownBy(() -> useCase.execute(new OperationFilter.All(), -1, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("page must not be negative");
  }

  @Test
  void refusesASizeOutsideTheAcceptedRange() {
    assertThatThrownBy(() -> useCase.execute(new OperationFilter.All(), 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be between 1 and 100");
    assertThatThrownBy(() -> useCase.execute(new OperationFilter.All(), 0, 101))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be between 1 and 100");
  }

  @Test
  void refusesAFilterThatDoesNotExist() {
    assertThatThrownBy(() -> useCase.execute(null, 0, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("filter must not be null");
  }

  private static PortabilityOperation operation(int index, OperationStatus status) {
    PortabilityOperation operation =
        PortabilityOperation.create(
                new PortabilityOperation.OperationId(
                    UUID.nameUUIDFromBytes(
                        ("operation-2.14-" + index)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))),
                new IdempotencyKey(
                    UUID.nameUUIDFromBytes(
                        ("key-2.14-" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8))),
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
                NOW.plusSeconds(index))
            .submit(PROTOCOL + "-" + index);
    return switch (status) {
      case PENDING -> operation;
      case COMPLETED -> operation.confirm().complete(NOW.plusSeconds(index));
      default -> operation;
    };
  }
}
