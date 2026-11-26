package br.com.consigport.architecture.fixture.adapters.in.web;

import br.com.consigport.architecture.fixture.adapters.out.ConcreteOutboundAdapter;

/**
 * Deliberate violation used to prove the inbound rule bites: an inbound adapter never
 * reaches into an outbound one. Never referenced by production code.
 */
public final class InboundUsingOutboundAdapter {

  /**
   * The dependency on a concrete outbound adapter is exactly what the rule must reject.
   *
   * @return a new concrete adapter
   */
  public ConcreteOutboundAdapter adapter() {
    return new ConcreteOutboundAdapter();
  }
}
