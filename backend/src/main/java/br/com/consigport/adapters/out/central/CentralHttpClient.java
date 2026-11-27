package br.com.consigport.adapters.out.central;

import br.com.consigport.adapters.out.central.acl.CentralAcl;
import br.com.consigport.adapters.out.central.acl.PortabilityAcl;
import br.com.consigport.adapters.out.central.dto.AgendaResponse;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityRequest;
import br.com.consigport.adapters.out.central.dto.CentralPortabilityResponse;
import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.agenda.BeneficiaryAgenda;
import br.com.consigport.domain.portability.CentralPortabilityPort;
import br.com.consigport.domain.portability.CentralSubmissionException;
import br.com.consigport.domain.portability.PortabilityOperation;
import br.com.consigport.domain.shared.BeneficiaryId;
import br.com.consigport.domain.shared.DomainError;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
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
 */
public final class CentralHttpClient implements AgendaGateway, CentralPortabilityPort {

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

  private final RestClient rest;
  private final HmacSigner signer;
  private final PortabilityAcl portabilityAcl;
  private final CentralAcl centralAcl;
  private final Clock clock;
  private final CentralProperties properties;

  /**
   * Builds the client.
   *
   * @param rest the transport towards the Central
   * @param signer the canonical form and the signature of the requests
   * @param portabilityAcl the translation of the submission of an operation
   * @param centralAcl the translation of the agenda
   * @param clock the clock that dates the signature
   * @param properties the configuration of the integration
   */
  public CentralHttpClient(
      RestClient rest,
      HmacSigner signer,
      PortabilityAcl portabilityAcl,
      CentralAcl centralAcl,
      Clock clock,
      CentralProperties properties) {
    this.rest = rest;
    this.signer = signer;
    this.portabilityAcl = portabilityAcl;
    this.centralAcl = centralAcl;
    this.clock = clock;
    this.properties = properties;
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
      return centralAcl.toAgenda(beneficiaryId, List.of(parcelas == null ? new AgendaResponse[0] : parcelas));
    } catch (RestClientResponseException exception) {
      throw agendaFailure(exception, beneficiaryId);
    } catch (RestClientException exception) {
      // The Central did not answer: the connection failed, the reading timed out or the
      // answer is not the contract. The agenda cannot be read, so the operation cannot
      // start (BR-04).
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
    throw failure;
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
