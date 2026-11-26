package br.com.consigport.config;

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
}
