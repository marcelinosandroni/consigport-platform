package br.com.consigport.adapters.out.persistence;

import br.com.consigport.domain.shared.TransactionRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the transaction boundary over Spring.
 *
 * <p>The class is not final on purpose: the transaction is applied by a proxy.
 */
@Component
public class SpringTransactionRunner implements TransactionRunner {

  @Override
  @Transactional
  public void run(Runnable work) {
    work.run();
  }
}
