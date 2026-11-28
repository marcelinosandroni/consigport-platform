package br.com.consigport.architecture.fixture.application;

import br.com.consigport.adapters.out.central.dto.AgendaResponse;

/**
 * Deliberate violation used to prove the DTO rule bites: the DTO of the Central never
 * leaves the outbound adapter. Never referenced by production code.
 */
public final class ApplicationUsingCentralDto {

  /**
   * The dependency on the DTO of the Central is exactly what the rule must reject.
   *
   * @return a DTO of the Central
   */
  public AgendaResponse dto() {
    return null;
  }
}
