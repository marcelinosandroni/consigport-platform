package br.com.consigport.domain.shared;

/**
 * Boundary of a transaction, so the application layer can persist more than one
 * aggregate atomically without knowing the framework.
 *
 * <p>The operation and its creation event are written in the same transaction (BR-12):
 * an operation without its event would break the trail of the audit. The port lives in
 * the domain because the guarantee belongs to it; the implementation belongs to the
 * persistence adapter.
 */
public interface TransactionRunner {

  /**
   * Runs the work inside a transaction.
   *
   * <p>The transaction is committed when the work returns and rolled back when it
   * throws.
   *
   * @param work the work to run
   */
  void run(Runnable work);
}
