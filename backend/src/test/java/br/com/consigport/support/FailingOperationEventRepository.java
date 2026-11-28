package br.com.consigport.support;

import br.com.consigport.domain.audit.OperationEvent;
import br.com.consigport.domain.audit.OperationEventRepository;
import br.com.consigport.domain.portability.PortabilityOperation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trail that fails on the nth append, to prove the rollback of the transaction.
 *
 * <p>The double counts only its own appends: the events written before the double was
 * installed do not move the counter. The failure is an {@link IllegalStateException}, the
 * kind of failure that rolls a transaction back.
 */
public final class FailingOperationEventRepository implements OperationEventRepository {

  private final OperationEventRepository delegate;
  private final int failingAppend;
  private final AtomicInteger appends = new AtomicInteger();

  /**
   * Builds the double.
   *
   * @param delegate the real trail, which receives the events that do not fail
   * @param failingAppend the number of the append that fails, counting from one
   */
  public FailingOperationEventRepository(OperationEventRepository delegate, int failingAppend) {
    this.delegate = delegate;
    this.failingAppend = failingAppend;
  }

  @Override
  public OperationEvent append(OperationEvent event) {
    if (appends.incrementAndGet() == failingAppend) {
      throw new IllegalStateException("the trail failed between the steps");
    }
    return delegate.append(event);
  }

  @Override
  public List<OperationEvent> findByOperationId(PortabilityOperation.OperationId operationId) {
    return delegate.findByOperationId(operationId);
  }
}
