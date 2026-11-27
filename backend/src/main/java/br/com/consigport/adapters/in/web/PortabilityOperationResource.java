package br.com.consigport.adapters.in.web;

import br.com.consigport.adapters.in.web.dto.ErrorResponse;
import br.com.consigport.adapters.in.web.dto.InstallmentSnapshotResponse;
import br.com.consigport.adapters.in.web.dto.OperationAcceptedResponse;
import br.com.consigport.adapters.in.web.dto.OperationDetailResponse;
import br.com.consigport.adapters.in.web.dto.OperationPageResponse;
import br.com.consigport.adapters.in.web.dto.OperationSummaryResponse;
import br.com.consigport.adapters.in.web.dto.PortabilityOperationRequest;
import br.com.consigport.adapters.in.web.dto.ReceivingAccount;
import br.com.consigport.application.portability.FindOperation;
import br.com.consigport.application.portability.ListOperations;
import br.com.consigport.application.portability.StartOperation;
import br.com.consigport.domain.portability.OperationDetail;
import br.com.consigport.domain.portability.OperationFilter;
import br.com.consigport.domain.portability.OperationRequest;
import br.com.consigport.domain.portability.OperationStatus;
import br.com.consigport.domain.portability.OperationSummary;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.shared.AgreementId;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.IdempotencyKey;
import br.com.consigport.domain.shared.InstallmentId;
import br.com.consigport.domain.shared.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public edge of the portability operations.
 *
 * <p>The start of the operation answers {@code 202}: the confirmation of the Central is
 * asynchronous. The header {@code Idempotent-Replay} tells the caller that the
 * operation already existed for the key that was sent.
 *
 * <p>The consultation is paginated and filtered by status; the detail of one operation
 * carries the snapshot of the request and the instants of the life of the operation.
 */
@RestController
@RequestMapping("/api/v1/portability-operations")
@Tag(
    name = "Portability Operations",
    description = "Criação, consulta e acompanhamento de operações de portabilidade.")
public class PortabilityOperationResource {

  /** The header that carries the key of the idempotency. */
  public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

  /** The header that tells the caller that the operation already existed. */
  public static final String IDEMPOTENT_REPLAY_HEADER = "Idempotent-Replay";

  /** The default size of the page of the consultation. */
  public static final int DEFAULT_PAGE_SIZE = 20;

  private final StartOperation startOperation;
  private final FindOperation findOperation;
  private final ListOperations listOperations;

  /**
   * Builds the resource.
   *
   * @param startOperation the use case that starts the operation
   * @param findOperation the use case that consults one operation
   * @param listOperations the use case that consults the operations, one page at a time
   */
  public PortabilityOperationResource(
      StartOperation startOperation,
      FindOperation findOperation,
      ListOperations listOperations) {
    this.startOperation = startOperation;
    this.findOperation = findOperation;
    this.listOperations = listOperations;
  }

  /**
   * Starts the operation.
   *
   * @param idempotencyKey the raw key of the idempotency
   * @param request the request of the operation
   * @return the accepted operation
   * @throws DomainError.IdempotencyKeyRequired when the key is missing
   * @throws DomainError.IdempotencyConflict when the key was used with another payload
   * @throws DomainError.InstallmentNotEligible when an installment is not eligible
   */
  @PostMapping
  @Operation(
      operationId = "startPortabilityOperation",
      summary = "Cria uma operação de portabilidade",
      description =
          """
          Cria a operação com snapshot das parcelas e envia a solicitação assinada à
          Central. A resposta é `202` porque a confirmação é assíncrona. A mesma
          `Idempotency-Key` com o mesmo payload devolve a operação existente e o
          header `Idempotent-Replay: true`; com payload diferente devolve `409`.
          """)
  @Parameter(
      name = IDEMPOTENCY_KEY_HEADER,
      in = ParameterIn.HEADER,
      required = true,
      description = "UUID que identifica a tentativa de criação da operação.")
  @ApiResponse(
      responseCode = "202",
      description = "Operação aceita e enviada à Central.",
      headers =
          @io.swagger.v3.oas.annotations.headers.Header(
              name = IDEMPOTENT_REPLAY_HEADER,
              description = "`true` quando a operação devolvida já existia para a chave enviada.",
              schema = @Schema(type = "boolean")),
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OperationAcceptedResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Requisição inválida.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "A chave de idempotência já foi usada com outro conteúdo.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "422",
      description = "Parcela inelegível para portabilidade.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "503",
      description = "Central de Convênios indisponível.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Erro interno inesperado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<OperationAcceptedResponse> start(
      @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
      @Valid @RequestBody PortabilityOperationRequest request) {
    StartOperation.StartOperationResult result =
        startOperation.execute(toDomainRequest(request), toIdempotencyKey(idempotencyKey));

    return ResponseEntity.accepted()
        .header(IDEMPOTENT_REPLAY_HEADER, Boolean.toString(result.replay()))
        .body(toResponse(result.operation()));
  }

  /**
   * Lists the operations, one page at a time.
   *
   * @param status the status of the operations, when the caller filters by it
   * @param page the zero-based index of the page
   * @param size the maximum number of operations of the page
   * @return the page of the operations, from the oldest to the newest
   * @throws IllegalArgumentException when the page or the size are outside the accepted
   *     range
   */
  @GetMapping
  @Operation(
      operationId = "listPortabilityOperations",
      summary = "Lista operações de portabilidade",
      description = "Lista paginada, opcionalmente filtrada por status.")
  @ApiResponse(
      responseCode = "200",
      description = "Página de operações.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OperationPageResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Requisição inválida.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Erro interno inesperado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  public OperationPageResponse list(
      @RequestParam(required = false) OperationStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    OperationFilter filter =
        status == null ? new OperationFilter.All() : new OperationFilter.WithStatus(status);
    Page<OperationSummary> result = listOperations.execute(filter, page, size);

    return new OperationPageResponse(
        result.items().stream().map(PortabilityOperationResource::toSummary).toList(),
        result.page(),
        result.size(),
        result.totalElements(),
        result.totalPages());
  }

  /**
   * Finds one operation.
   *
   * @param operationId the identity of the operation
   * @return the detail of the operation
   * @throws DomainError.OperationNotFound when the operation does not exist
   */
  @GetMapping("/{operationId}")
  @Operation(
      operationId = "findPortabilityOperation",
      summary = "Consulta uma operação de portabilidade",
      description =
          "Devolve o detalhe completo, incluindo snapshot, conta e motivo de rejeição.")
  @ApiResponse(
      responseCode = "200",
      description = "Operação encontrada.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OperationDetailResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Recurso não encontrado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Erro interno inesperado.",
      content = @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ErrorResponse.class)))
  public OperationDetailResponse find(@PathVariable String operationId) {
    OperationDetail detail =
        findOperation.execute(PortabilityOperation.OperationId.parse(operationId));

    return toDetail(detail);
  }

  private static IdempotencyKey toIdempotencyKey(String rawKey) {
    if (rawKey == null || rawKey.isBlank()) {
      throw new DomainError.IdempotencyKeyRequired();
    }
    return IdempotencyKey.parse(rawKey);
  }

  private static OperationRequest toDomainRequest(PortabilityOperationRequest request) {
    return new OperationRequest(
        new BeneficiaryId(request.beneficiaryId()),
        new AgreementId(request.agreementId()),
        request.installmentIds().stream().map(InstallmentId::new).toList(),
        request.assigneeCreditor(),
        // The value object of the domain and the DTO of the edge share the name: the
        // domain one is qualified to say which of the two the operation carries.
        new br.com.consigport.domain.portability.ReceivingAccount(
            request.receivingAccount().bank(),
            request.receivingAccount().branch(),
            request.receivingAccount().account()));
  }

  private static OperationAcceptedResponse toResponse(PortabilityOperation operation) {
    return new OperationAcceptedResponse(
        operation.id().value().toString(),
        operation.protocol(),
        operation.status(),
        operation.totalAmount().amount(),
        operation.createdAt());
  }

  private static OperationSummaryResponse toSummary(OperationSummary summary) {
    return new OperationSummaryResponse(
        summary.id().value().toString(),
        summary.protocol(),
        summary.beneficiaryId().value(),
        summary.status(),
        summary.totalAmount().amount(),
        summary.createdAt(),
        summary.updatedAt());
  }

  private static OperationDetailResponse toDetail(OperationDetail detail) {
    return new OperationDetailResponse(
        detail.id().value().toString(),
        detail.protocol(),
        detail.beneficiaryId().value(),
        detail.status(),
        detail.totalAmount().amount(),
        detail.createdAt(),
        detail.updatedAt(),
        detail.agreementId().value(),
        detail.installments().stream()
            .map(
                installment ->
                    new InstallmentSnapshotResponse(
                        installment.installmentId().value(),
                        installment.agreementId().value(),
                        installment.amount().amount(),
                        installment.discountDate()))
            .toList(),
        detail.assigneeCreditor(),
        new ReceivingAccount(
            detail.receivingAccount().bank(),
            detail.receivingAccount().branch(),
            detail.receivingAccount().account()),
        detail.rejectionReason() == null ? null : detail.rejectionReason().code(),
        detail.retryCount(),
        detail.accountUpdatedAt());
  }
}
