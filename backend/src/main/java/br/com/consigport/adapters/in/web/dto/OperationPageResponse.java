package br.com.consigport.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * One page of the operations, as the public API answers it.
 *
 * @param items the operations of the page
 * @param page the zero-based index of the page
 * @param size the maximum number of operations of the page
 * @param totalElements the total of operations
 * @param totalPages the total of pages
 */
@Schema(
    name = "OperationPage",
    requiredProperties = {"items", "page", "size", "totalElements", "totalPages"})
public record OperationPageResponse(
    List<OperationSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages) {}
