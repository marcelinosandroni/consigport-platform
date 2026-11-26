package br.com.consigport.domain.agenda;

/**
 * Situation of an installment in the agenda of a beneficiary.
 *
 * <p>Only {@code OPEN} installments are eligible for a portability operation (BR-03).
 */
public enum InstallmentStatus {

  /** The installment is open and can be chosen. */
  OPEN,

  /** The installment is closed and cannot be chosen. */
  CLOSED
}
