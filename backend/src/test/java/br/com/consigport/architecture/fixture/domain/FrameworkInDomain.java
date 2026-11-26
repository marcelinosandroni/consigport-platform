package br.com.consigport.architecture.fixture.domain;

import org.springframework.stereotype.Component;

/**
 * Deliberate violation used to prove the domain rule bites: the domain must not know
 * Spring. Never referenced by production code.
 */
@Component
public final class FrameworkInDomain {

  /**
   * The return type alone is enough for ArchUnit to record the dependency.
   *
   * @return always {@code null}
   */
  public Component component() {
    return null;
  }
}
