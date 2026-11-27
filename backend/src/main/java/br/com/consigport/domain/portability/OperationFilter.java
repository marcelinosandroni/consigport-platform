package br.com.consigport.domain.portability;

/**
 * Filter of the consultation of the portability operations.
 *
 * <p>The hierarchy is sealed: the consultation either lists every operation or the
 * operations of one status. There is no {@code null} that means "no filter".
 */
public sealed interface OperationFilter {

  /**
   * Lists every operation, whatever its status.
   */
  record All() implements OperationFilter {}

  /**
   * Lists the operations of one status.
   *
   * @param status the status of the operations
   */
  record WithStatus(OperationStatus status) implements OperationFilter {

    /**
     * Validates the filter.
     *
     * @param status the status of the operations
     * @throws IllegalArgumentException when the status is {@code null}
     */
    public WithStatus {
      if (status == null) {
        throw new IllegalArgumentException("status must not be null");
      }
    }
  }
}
