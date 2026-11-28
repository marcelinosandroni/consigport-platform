package br.com.consigport.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contract of the {@link OperationEventRepository} port.
 *
 * <p>The trail only grows: the port exposes the append and the queries, and nothing that
 * could change or remove an event.
 */
class OperationEventRepositoryContractTest {

  private static final PortabilityOperation.OperationId OPERATION_ID =
      new PortabilityOperation.OperationId(
          UUID.fromString("c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601"));

  private static final PortabilityOperation.OperationId OTHER_OPERATION_ID =
      new PortabilityOperation.OperationId(
          UUID.fromString("f0e1d2c3-b4a5-4968-8778-99aabbccddee"));

  private static final Instant FIRST = Instant.parse("2026-11-23T12:00:05Z");
  private static final Instant SECOND = Instant.parse("2026-11-23T12:00:35Z");

  private InMemoryOperationEventRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryOperationEventRepository();
  }

  @Test
  void appendsTheEventAndAssignsTheSequentialIdentifier() {
    OperationEvent stored = repository.append(creationEvent(OPERATION_ID, FIRST));

    assertThat(stored.eventId()).isEqualTo(1L);
    assertThat(repository.findByOperationId(OPERATION_ID)).containsExactly(stored);
  }

  @Test
  void numbersTheEventsInOrder() {
    repository.append(creationEvent(OPERATION_ID, FIRST));
    OperationEvent second = repository.append(submissionEvent(OPERATION_ID, SECOND));

    assertThat(second.eventId()).isEqualTo(2L);
  }

  @Test
  void listsTheEventsInChronologicalOrder() {
    repository.append(submissionEvent(OPERATION_ID, SECOND));
    repository.append(creationEvent(OPERATION_ID, FIRST));

    assertThat(repository.findByOperationId(OPERATION_ID))
        .extracting(OperationEvent::eventType)
        .containsExactly(EventType.OPERATION_CREATED, EventType.OPERATION_SUBMITTED);
  }

  @Test
  void returnsOnlyTheEventsOfTheGivenOperation() {
    repository.append(creationEvent(OPERATION_ID, FIRST));
    repository.append(creationEvent(OTHER_OPERATION_ID, FIRST));

    assertThat(repository.findByOperationId(OPERATION_ID)).hasSize(1);
    assertThat(repository.findByOperationId(OTHER_OPERATION_ID)).hasSize(1);
  }

  @Test
  void returnsAnEmptyTrailForAnUnknownOperation() {
    assertThat(repository.findByOperationId(OPERATION_ID)).isEmpty();
  }

  @Test
  void exposesOnlyTheAppendAndTheQueries() {
    Set<String> methods =
        Arrays.stream(OperationEventRepository.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertThat(methods).containsExactlyInAnyOrder("append", "findByOperationId");
  }

  private static OperationEvent creationEvent(
      PortabilityOperation.OperationId operationId, Instant createdAt) {
    return OperationEvent.record(
        operationId,
        null,
        OperationStatus.CREATED,
        EventType.OPERATION_CREATED,
        "Operation created",
        null,
        createdAt);
  }

  private static OperationEvent submissionEvent(
      PortabilityOperation.OperationId operationId, Instant createdAt) {
    return OperationEvent.record(
        operationId,
        OperationStatus.CREATED,
        OperationStatus.PENDING,
        EventType.OPERATION_SUBMITTED,
        "Operation submitted to the Central",
        null,
        createdAt);
  }
}
