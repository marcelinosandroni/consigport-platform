package br.com.consigport.architecture.fixture.adapters.out;

/**
 * Deliberate concrete outbound adapter, used to prove the composition-root rule bites.
 * Never referenced by production code.
 */
public final class ConcreteOutboundAdapter {

  /**
   * Returns a fixed value; the fixture never runs.
   *
   * @return the word {@code fixture}
   */
  public String call() {
    return "fixture";
  }
}
