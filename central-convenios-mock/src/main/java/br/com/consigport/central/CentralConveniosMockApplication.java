package br.com.consigport.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Simulated Central de Convênios.
 *
 * <p>Speaks the edital contract in Portuguese, holds its state in memory and is
 * reset by the admin endpoints so every test starts from the same fixtures.
 */
@SpringBootApplication
public class CentralConveniosMockApplication {

  public static void main(String[] args) {
    SpringApplication.run(CentralConveniosMockApplication.class, args);
  }
}
