package br.com.consigport.adapters.out.central;

import br.com.consigport.adapters.out.central.acl.CentralAcl;
import br.com.consigport.adapters.out.central.acl.PortabilityAcl;
import br.com.consigport.adapters.out.central.dto.AgendaResponse;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityRequest;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityResponse;
import br.com.consigport.adapters.out.resilience.CircuitBreaker;
import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.portability.CentralPortabilityPort;
import br.com.consigport.domain.portability.CentralStatus;
import br.com.consigport.domain.portability.CentralStatusPort;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Client of the Central de Convênios.
 *
 * <p>Every request is signed with HMAC-SHA256 over the canonical form of the request, as
 * {@code contracts/hmac-vector.json} and ADR {@code 0002} define them. The client speaks
 * the contract of the Central (Portuguese) and hands the domain the English it
 * understands; the translation belongs to the anti-corruption layers.
 *
 * <p>The submission of an operation is retried: the Central answers {@code 202} with the
 * protocol and confirms later (BR-08). A submission that fails after the attempts leaves
 * the operation resumable, never duplicated (BR-11, BR-15): every attempt carries the
 * same idempotency key.
 *
 * <p>Every call goes through the circuit: a Central that is failing is not hammered, and
 * the calls are refused while the circuit is open ({@code ARCHITECTURE.md} §11).
 */
public final class CentralHttpClient
    implements AgendaGateway, CentralPortabilityPort, CentralStatusPort {

  /** The path of the submission of an operation. */
  private static final String PORTABILITY_PATH = "/operacoes-portabilidade";

  /** The prefix of the path of the agenda. */
  private static final String AGENDA_PATH_PREFIX = "/beneficiarios/";

  /** The suffix of the path of the agenda. */
  private static final String AGENDA_PATH_SUFFIX = "/agenda";

  /** The header that carries the signature. */
  private static final String SIGNATURE_HEADER = "X-Signature";

  /** The header that carries the instant of the signature. */
  private static final String TIMESTAMP_HEADER = "X-Timestamp";

  /** The header that carries the key of the idempotency. */
  private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

  /** The header that carries the interval the Central asks for. */
  private static final String RETRY_AFTER_HEADER = "Retry-After";

  private final RestClient rest;
  private final HmacSigner signer;
  private final PortabilityAcl portabilityAcl;
  private final CentralAcl centralAcl;
  private final Clock clock;
  private final CentralProperties properties;
  private final CircuitBreaker circuit;

  /**
   * Builds the client.
   *
   * @param rest the transport towards the Central
   * @param signer the canonical form and the signature of the requests
   * @param portabilityAcl the translation of the submission of an operation
   * @param centralAcl the translation of the agenda
   * @param clock the clock that dates the signature
   * @param properties the configuration of the integration
   * @param circuit the circuit that protects the calls to the Central
   */
  public CentralHttpClient(
      RestClient rest,
      HmacSigner signer,
      PortabilityAcl portabilityAcl,
      CentralAcl centralAcl,
      Clock clock,
      CentralProperties properties,
      CircuitBreaker circuit) {
    this.rest = rest;
    this.signer = signer;
    this.portabilityAcl = portabilityAcl;
    this.centralAcl = centralAcl;
    this.clock = clock;
    this.properties = properties;
    this.circuit = circuit;
  }

  /**
   * Reads the agenda of a beneficiary.
   *
   * @param beneficiaryId the beneficiary whose agenda is read
   * @return the agenda of the beneficiary
   * @throws DomainError.AuthorizationRequired when the beneficiary has no opt-in
   * @throws DomainError.BeneficiaryNotFound when the Central does not know the beneficiary
   * @throws DomainError.CentralUnavailable when the Central does not answer
   */
  @Override
  public BeneficiaryAgenda findAgenda(BeneficiaryId beneficiaryId) {
    requireClosedCircuit();
    String path = AGENDA_PATH_PREFIX + beneficiaryId.value() + AGENDA_PATH_SUFFIX;
    long timestamp = clock.instant().getEpochSecond();
    String signature = signer.sign("GET", path, timestamp, "");
    try {
      AgendaResponse[] parcelas =
          rest.get()
              .uri(path)
              .header(SIGNATURE_HEADER, signature)
              .header(TIMESTAMP_HEADER, Long.toString(timestamp))
              .retrieve()
              .body(AgendaResponse[].class);
      circuit.recordSuccess();
      return centralAcl.toAgenda(beneficiaryId, List.of(parcelas == null ? new AgendaResponse[0] : parcelas));
    } catch (RestClientResponseException exception) {
      circuit.recordFailure();
      throw agendaFailure(exception, beneficiaryId);
    } catch (RestClientException exception) {
      circuit.recordFailure();
      throw new DomainError.CentralUnavailable();
    }
  }

  /**
   * Submits the operation to the Central.
   *
   * @param operation the operation to submit
   * @return the protocol returned by the Central
   * @throws CentralSubmissionException when the Central does not accept the operation
   */
  @Override
  public String submit(PortabilityOperation operation) {
    requireClosedCircuit();
    CentralPortabilityRequest request = portabilityAcl.toCentralRequest(operation);
    String canonicalBody = signer.canonicalJson(request);
    CentralSubmissionException failure = null;

    for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
      long timestamp = clock.instant().getEpochSecond();
      String signature = signer.sign("POST", PORTABILITY_PATH, timestamp, canonicalBody);
      String body = signer.canonicalJson(portabilityAcl.withSignature(request, signature));
      try {
        CentralPortabilityResponse response =
            rest.post()
                .uri(PORTABILITY_PATH)
                .header(SIGNATURE_HEADER, signature)
                .header(TIMESTAMP_HEADER, Long.toString(timestamp))
                .header(IDEMPOTENCY_HEADER, operation.idempotencyKey().value().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(CentralPortabilityResponse.class);
        circuit.recordSuccess();
        return portabilityAcl.toProtocol(response);
      } catch (RestClientResponseException exception) {
        failure =
            portabilityAcl.toSubmissionFailure(
                exception.getStatusCode().value(),
                exception.getResponseBodyAsString(),
                exception.getMessage());
        if (!isRetryable(exception.getStatusCode().value())) {
          break;
        }
      } catch (RestClientException exception) {
        // The transport failed: the connection was refused, the reading of the answer
        // timed out or the answer is not the contract of the Central. The operation is
        // not lost: it stays resumable and the next attempt carries the same idempotency
        // key, so the Central never receives the operation twice (BR-11, BR-15).
        failure = new CentralSubmissionException(exception.getMessage());
      } catch (IllegalArgumentException exception) {
        failure =
            new CentralSubmissionException(
                202, "ERRO_CENTRAL", "the Central answered without a protocol");
      }
      if (attempt < properties.maxAttempts()) {
        waitBeforeRetry(attempt);
      }
    }
    circuit.recordFailure();
    throw failure;
  }

  /**
   * Reads the situation of the operation in the Central.
   *
   * <p>The answer of the Central has four shapes: {@code 204} with {@code Retry-After}
   * while the operation is still processed, {@code 200 CONFIRMADA} when it ends well,
   * {@code 200 REJEITADA} when it ends with a reason and {@code 404} when it does not
   * know the protocol.
   *
   * <p>The {@code 404} of the contract ({@code OPERACAO_NAO_ENCONTRADA}) is not an
   * unavailability of the Central: the Central answered, so it is available and the
   * circuit records the call as successful. The protocol may exist again, so the answer
   * is returned to the application, which keeps the operation resumable.
   *
   * @param protocol the protocol of the operation
   * @return the situation of the operation
   * @throws DomainError.CentralUnavailable when the Central does not answer
   */
  @Override
  public CentralStatus statusOf(String protocol) {
    requireClosedCircuit();
    String path = PORTABILITY_PATH + "/" + protocol;
    String failure = "the Central did not answer the status of the operation";
    for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
      long timestamp = clock.instant().getEpochSecond();
      String signature = signer.sign("GET", path, timestamp, "");
      try {
        ResponseEntity<String> response =
            rest.get()
                .uri(path)
                .header(SIGNATURE_HEADER, signature)
                .header(TIMESTAMP_HEADER, Long.toString(timestamp))
                .retrieve()
                .toEntity(String.class);
        if (response.getStatusCode().value() == 204) {
          circuit.recordSuccess();
          return new CentralStatus.Pending(retryAfter(response), null);
        }
        CentralStatus answer = portabilityAcl.toCentralStatus(response.getBody(), properties.pollInterval());
        circuit.recordSuccess();
        return answer;
      } catch (RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (status == 404) {
          // The Central answered: it is available. It simply does not know the protocol.
          circuit.recordSuccess();
          return new CentralStatus.NotFound(protocol, exception.getResponseBodyAsString());
        }
        if (!isRetryable(status)) {
          failure = "the Central refused the status of the operation with " + status;
          break;
        }
      } catch (RestClientException exception) {
        // The transport failed: the next attempt reads the status again.
      } catch (IllegalArgumentException exception) {
        // The answer of the Central is not the contract: reading it again would fail
        // the same way.
        failure = "the Central answered a body that is not the contract of the status";
        break;
      }
      if (attempt < properties.maxAttempts()) {
        waitBeforeRetry(attempt);
      }
    }
    circuit.recordFailure();
    throw new DomainError.CentralUnavailable(failure);
  }

  /**
   * Refuses the call while the circuit is open.
   *
   * @throws DomainError.CentralUnavailable when the circuit is open
   */
  private void requireClosedCircuit() {
    if (!circuit.allowsCall()) {
      throw new DomainError.CentralUnavailable();
    }
  }

  /**
   * The interval the Central asks for, in the {@code Retry-After} header.
   *
   * <p>The header carries seconds. A missing or unreadable header falls back to the
   * configured interval of the polling.
   *
   * @param response the answer of the Central
   * @return the interval before the next poll
   */
  private Duration retryAfter(ResponseEntity<String> response) {
    String header = response.getHeaders().getFirst(RETRY_AFTER_HEADER);
    if (header == null || header.isBlank()) {
      return properties.pollInterval();
    }
    try {
      long seconds = Long.parseLong(header.trim());
      return seconds < 0 ? properties.pollInterval() : Duration.ofSeconds(seconds);
    } catch (NumberFormatException exception) {
      return properties.pollInterval();
    }
  }

  private DomainError agendaFailure(
      RestClientResponseException exception, BeneficiaryId beneficiaryId) {
    int status = exception.getStatusCode().value();
    if (status == 403 || status == 404) {
      return centralAcl.toDomainError(status, beneficiaryId);
    }
    return new DomainError.CentralUnavailable();
  }

  private static boolean isRetryable(int status) {
    return status == 408 || status == 429 || status >= 500;
  }

  private void waitBeforeRetry(int attempt) {
    Duration delay = properties.retryDelay().multipliedBy(1L << (attempt - 1));
    try {
      Thread.sleep(delay.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new CentralSubmissionException("the submission was interrupted");
    }
  }
}
