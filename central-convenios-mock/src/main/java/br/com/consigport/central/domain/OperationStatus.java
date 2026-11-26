package br.com.consigport.central.domain;

/**
 * Lifecycle of a portability operation in the simulated Central.
 *
 * <p>The names mirror the edital contract ({@code contracts/central-api.yaml}) and are
 * serialized verbatim in Portuguese.
 */
public enum OperationStatus {
  PENDENTE,
  CONFIRMADA,
  REJEITADA
}
