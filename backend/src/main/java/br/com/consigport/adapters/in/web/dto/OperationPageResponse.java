package br.com.consigport.adapters.in.web.dto;

import java.util.List;

/**
 * Answer of the consultation of the operations, one page at a time.
 *
 * <p>The shape is the one of the public contract
 * ({@code contracts/consigport-api.yaml}). The total says how many operations the whole
 * result set has, so the caller knows how many pages there are without loading them all.
 *
 * @param items the operations of the page, from the oldest to the newest
 * @param page the zero-based index of the page
 * @param size the maximum number of operations of the page
 * @param totalElements how many operations the whole result set has
 * @param totalPages how many pages the whole result set has
 */
public record OperationPageResponse(
    List<OperationSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages) {}
