package br.com.consigport.domain.shared;

import java.util.List;

/**
 * One page of a result set, with the total that the caller needs to navigate.
 *
 * <p>The page is immutable: the items are copied when the page is built, so a caller
 * cannot change what the storage returned.
 *
 * @param items the items of the page, from the first to the last
 * @param page the zero-based index of the page
 * @param size the maximum number of items of the page
 * @param totalElements how many items the whole result set has
 * @param <T> the type of the items
 */
public record Page<T>(List<T> items, int page, int size, long totalElements) {

  /**
   * Validates the page.
   *
   * @param items the items of the page
   * @param page the zero-based index of the page
   * @param size the maximum number of items of the page
   * @param totalElements how many items the whole result set has
   * @throws IllegalArgumentException when any invariant is broken
   */
  public Page {
    if (page < 0) {
      throw new IllegalArgumentException("page must not be negative");
    }
    if (size < 1) {
      throw new IllegalArgumentException("size must be positive");
    }
    if (items == null) {
      throw new IllegalArgumentException("items must not be null");
    }
    items = List.copyOf(items);
    if (items.size() > size) {
      throw new IllegalArgumentException("items must not exceed size");
    }
    if (totalElements < 0) {
      throw new IllegalArgumentException("totalElements must not be negative");
    }
    if (items.size() > totalElements) {
      throw new IllegalArgumentException("items must not exceed totalElements");
    }
  }

  /**
   * How many pages the whole result set has.
   *
   * <p>An empty result set has no page at all.
   *
   * @return the number of pages
   */
  public int totalPages() {
    if (totalElements == 0) {
      return 0;
    }
    return (int) ((totalElements + size - 1) / size);
  }

  /**
   * Whether the page is the last one of the result set.
   *
   * @return {@code true} when there is no page after this one
   */
  public boolean isLast() {
    return page >= totalPages() - 1;
  }
}
