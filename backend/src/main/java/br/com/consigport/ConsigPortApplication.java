package br.com.consigport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root of the ConsigPort backend.
 *
 * <p>Domain code stays free of Spring; this class only boots the adapters that
 * the framework wires around it.
 */
@SpringBootApplication
public class ConsigPortApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsigPortApplication.class, args);
  }
}
