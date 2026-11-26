package br.com.consigport.config;

import br.com.consigport.application.portability.StartOperation;
import br.com.consigport.domain.agenda.AgendaGateway;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.OperationPolicy;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.DomainError;
import br.com.consigport.domain.shared.TransactionRunner;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
   * The agenda of the beneficiary, read in the Central.
   *
   * <p>The HTTP client of the Central arrives with task 2.10, together with the signer
   * that the Central requires. Until then the agenda is unavailable and the start of the
   * operation answers {@code 503 CENTRAL_UNAVAILABLE}.
   *
   * @return the gateway of the agenda
   */
  @Bean
  public AgendaGateway agendaGateway() {
    return beneficiaryId -> {
      throw new DomainError.CentralUnavailable();
    };
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
   * @param events the append-only trail of the audit
   * @param policy the rule of the eligibility of the installments
   * @param transactions the boundary of the transaction of the creation
   * @param clock the clock that dates the creation
   * @return the use case
   */
  @Bean
  public StartOperation startOperation(
      PortabilityOperationRepository operations,
      AgendaGateway agenda,
      OperationEventRepository events,
      OperationPolicy policy,
      TransactionRunner transactions,
      Clock clock) {
    return new StartOperation(operations, agenda, events, policy, transactions, clock);
  }
}
