package br.com.consigport.config;

import br.com.consigport.adapters.out.central.CentralHttpClient;
import br.com.consigport.adapters.out.central.CentralProperties;
import br.com.consigport.adapters.out.central.HmacSigner;
import br.com.consigport.adapters.out.central.acl.CentralAcl;
import br.com.consigport.adapters.out.central.acl.PortabilityAcl;
import br.com.consigport.application.portability.StartOperation;
import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.CentralPortabilityPort;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.TransactionRunner;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Composition root of the ConsigPort backend.
 *
 * <p>Every concrete adapter is instantiated here and nowhere else, so the domain and the
 * application layers stay free of framework types. The class wires beans; it never carries
 * business logic.
 */
@Configuration
public class CompositionRoot {

  /**
   * The clock is a port: the domain receives it and never reads the system clock itself.
   *
   * @return the UTC clock used by the use cases
   */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  /**
   * The configuration of the integration with the Central.
   *
   * @param baseUrl the base URL of the Central
   * @param hmacSecret the shared secret of the HMAC
   * @param maxAttempts how many times a submission is attempted
   * @param retryDelay the delay between two attempts
   * @param connectTimeout the timeout of the connection
   * @param readTimeout the timeout of the reading
   * @return the configuration of the integration
   */
  @Bean
  public CentralProperties centralProperties(
      @Value("${central.base-url:http://localhost:8081}") String baseUrl,
      @Value("${central.hmac.secret:consigport-hmac-test-secret-2026}") String hmacSecret,
      @Value("${central.retry.max-attempts:3}") int maxAttempts,
      @Value("${central.retry.delay:500ms}") Duration retryDelay,
      @Value("${central.timeout.connect:2s}") Duration connectTimeout,
      @Value("${central.timeout.read:3s}") Duration readTimeout) {
    return new CentralProperties(
        baseUrl, hmacSecret, maxAttempts, retryDelay, connectTimeout, readTimeout);
  }

  /**
   * The canonical form and the signature of the requests sent to the Central.
   *
   * @param properties the configuration of the integration
   * @return the signer
   */
  @Bean
  public HmacSigner hmacSigner(CentralProperties properties) {
    return new HmacSigner(properties.hmacSecret());
  }

  /**
   * The transport towards the Central.
   *
   * @param properties the configuration of the integration
   * @return the client of the Central
   */
  @Bean
  public RestClient centralRestClient(CentralProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(properties.connectTimeout());
    factory.setReadTimeout(properties.readTimeout());
    return RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
  }

  /**
   * The anti-corruption layer of the agenda.
   *
   * @return the layer
   */
  @Bean
  public CentralAcl centralAcl() {
    return new CentralAcl();
  }

  /**
   * The anti-corruption layer of the submission of an operation.
   *
   * @return the layer
   */
  @Bean
  public PortabilityAcl portabilityAcl() {
    return new PortabilityAcl();
  }

  /**
   * The client of the Central: it reads the agenda and submits the operation.
   *
   * <p>The bean answers the two ports of the domain, {@code AgendaGateway} and
   * {@code CentralPortabilityPort}.
   *
   * @param rest the transport towards the Central
   * @param signer the canonical form and the signature of the requests
   * @param portabilityAcl the translation of the submission of an operation
   * @param centralAcl the translation of the agenda
   * @param clock the clock that dates the signature
   * @param properties the configuration of the integration
   * @return the client of the Central
   */
  @Bean
  public CentralHttpClient centralHttpClient(
      RestClient rest,
      HmacSigner signer,
      PortabilityAcl portabilityAcl,
      CentralAcl centralAcl,
      Clock clock,
      CentralProperties properties) {
    return new CentralHttpClient(rest, signer, portabilityAcl, centralAcl, clock, properties);
  }

  /**
   * The policy of the eligibility of the installments.
   *
   * @return the policy
   */
  @Bean
  public OperationPolicy operationPolicy() {
    return new OperationPolicy();
  }

  /**
   * The start of a portability operation.
   *
   * @param operations the storage of the operations
   * @param agenda the read of the agenda in the Central
   * @param central the submission of the operation to the Central
   * @param events the append-only trail of the audit
   * @param policy the rule of the eligibility of the installments
   * @param transactions the boundary of the transaction of the creation
   * @param clock the clock that dates the creation
   * @param integrationBackoff how long the operation waits before the next attempt
   * @return the use case
   */
  @Bean
  public StartOperation startOperation(
      PortabilityOperationRepository operations,
      AgendaGateway agenda,
      CentralPortabilityPort central,
      OperationEventRepository events,
      OperationPolicy policy,
      TransactionRunner transactions,
      Clock clock,
      @Value("${central.integration.backoff:60s}") Duration integrationBackoff) {
    return new StartOperation(
        operations,
        agenda,
        central,
        events,
        policy,
        transactions,
        clock,
        integrationBackoff);
  }
}
