package br.com.consigport.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.consigport.application.audit.ListOperationEvents;
import br.com.consigport.domain.audit.EventType;
import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.support.FixedClockConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The public edge of the trail of the audit of one operation.
 *
 * <p>The slice loads the resource and the advice of the errors: the use case is replaced
 * by a double, so the test proves the wire — a trilha em ordem cronológica, o payload da
 * Central como objeto e o `404` da operação que não existe.
 */
@WebMvcTest(OperationEventResource.class)
@Import(FixedClockConfig.class)
class OperationEventResourceTest {

  private static final UUID OPERATION = UUID.fromString("c1a2b3c4-d5e6-4f70-8a91-b2c3d4e5f601");

  @Autowired private MockMvc mvc;

  @MockitoBean private ListOperationEvents listOperationEvents;

  @Test
  void answersTheTrailInChronologicalOrder() throws Exception {
    when(listOperationEvents.execute(any(PortabilityOperation.OperationId.class)))
        .thenReturn(trail());

    mvc.perform(get("/api/v1/portability-operations/{id}/events", OPERATION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operationId").value(OPERATION.toString()))
        .andExpect(jsonPath("$.events.length()").value(3))
        .andExpect(jsonPath("$.events[0].eventType").value("OPERATION_CREATED"))
        .andExpect(jsonPath("$.events[0].previousStatus").doesNotExist())
        .andExpect(jsonPath("$.events[0].newStatus").value("CREATED"))
        .andExpect(jsonPath("$.events[1].eventType").value("OPERATION_SUBMITTED"))
        .andExpect(jsonPath("$.events[2].eventType").value("OPERATION_CONFIRMED"))
        .andExpect(jsonPath("$.events[2].rawPayload.status").value("CONFIRMADA"))
        .andExpect(jsonPath("$.events[2].rawPayload.motivoRejeicao").doesNotExist());
  }

  @Test
  void keepsTheBodyOfTheCentralThatIsNotAJsonObject() throws Exception {
    when(listOperationEvents.execute(any(PortabilityOperation.OperationId.class)))
        .thenReturn(
            List.of(
                OperationEvent.record(
                    new PortabilityOperation.OperationId(OPERATION),
                    OperationStatus.PENDING,
                    OperationStatus.PENDING,
                    EventType.POLL_ATTEMPT,
                    "The Central is still processing the operation",
                    "OPERACAO_NAO_ENCONTRADA",
                    Instant.parse("2026-11-27T11:30:00Z"))));

    mvc.perform(get("/api/v1/portability-operations/{id}/events", OPERATION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events[0].rawPayload.body").value("OPERACAO_NAO_ENCONTRADA"));
  }

  @Test
  void answersNotFoundWhenTheOperationDoesNotExist() throws Exception {
    when(listOperationEvents.execute(any(PortabilityOperation.OperationId.class)))
        .thenThrow(new DomainError.OperationNotFound(OPERATION));

    mvc.perform(get("/api/v1/portability-operations/{id}/events", OPERATION))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("OPERATION_NOT_FOUND"))
        .andExpect(jsonPath("$.locale").value("pt-BR"));
  }

  private static List<OperationEvent> trail() {
    PortabilityOperation.OperationId id = new PortabilityOperation.OperationId(OPERATION);
    return List.of(
        OperationEvent.record(
            id,
            null,
            OperationStatus.CREATED,
            EventType.OPERATION_CREATED,
            "Operation created",
            null,
            Instant.parse("2026-11-27T11:00:00Z")),
        OperationEvent.record(
            id,
            OperationStatus.CREATED,
            OperationStatus.PENDING,
            EventType.OPERATION_SUBMITTED,
            "Central accepted the operation",
            null,
            Instant.parse("2026-11-27T11:00:01Z")),
        OperationEvent.record(
            id,
            OperationStatus.PENDING,
            OperationStatus.CONFIRMED,
            EventType.OPERATION_CONFIRMED,
            "The Central confirmed the operation",
            "{\"status\":\"CONFIRMADA\",\"motivoRejeicao\":null}",
            Instant.parse("2026-11-27T11:00:10Z")));
  }
}
