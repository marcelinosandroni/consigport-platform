package br.com.consigport.architecture.fixture.application;

import br.com.consigport.architecture.fixture.adapters.out.ConcreteOutboundAdapter;

/**
 * Deliberate violation used to prove the application rules bite: the application layer may
 * know the domain only, never a concrete adapter. Never referenced by production code.
 */
public final class ApplicationUsingConcreteAdapter {

  /**
   * The dependency on a concrete adapter is exactly what the rules must reject.
   *
   * @return a new concrete adapter
   */
  public ConcreteOutboundAdapter adapter() {
    return new ConcreteOutboundAdapter();
  }
}
