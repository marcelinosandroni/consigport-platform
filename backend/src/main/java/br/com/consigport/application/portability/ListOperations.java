package br.com.consigport.application.portability;

import br.com.consigport.domain.portability.OperationFilter;
import br.com.consigport.domain.portability.OperationSummary;
import br.com.consigport.domain.portability.PortabilityOperationRepository;
import br.com.consigport.domain.shared.Page;

/**
 * Lists the portability operations, one page at a time.
 *
 * <p>The consultation is paginated and filtered by status: the page carries the total of
 * the result set, so the caller knows how many pages there are without loading them all.
 */
public final class ListOperations {

  /** The biggest page the API accepts. */
  public static final int MAX_PAGE_SIZE = 100;

  private final PortabilityOperationRepository operations;

  /**
   * Builds the use case.
   *
   * @param operations the storage of the operations
   */
  public ListOperations(PortabilityOperationRepository operations) {
    this.operations = operations;
  }

  /**
   * Lists the page of the operations.
   *
   * @param filter the filter of the consultation
   * @param page the zero-based index of the page
   * @param size the maximum number of operations of the page
   * @return the page of the operations, from the oldest to the newest
   * @throws IllegalArgumentException when the page or the size are outside the accepted
   *     range
   */
  public Page<OperationSummary> execute(OperationFilter filter, int page, int size) {
    if (filter == null) {
      throw new IllegalArgumentException("filter must not be null");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page must not be negative");
    }
    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
    }
    return operations.list(filter, page, size);
  }
}
