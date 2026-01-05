package com.ecommerce.common.dto;

import java.util.List;

/**
 * Page response for paginated data.
 * Uses Java 21 record for immutability.
 *
 * @param content       List of items in this page
 * @param page          Current page number (0-indexed)
 * @param size          Page size
 * @param totalElements Total number of elements
 * @param totalPages    Total number of pages
 * @param first         Whether this is the first page
 * @param last          Whether this is the last page
 * @param <T>           Type of the content items
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    /**
     * Create from Spring's Page object properties.
     */
    public static <T> PageResponse<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1);
    }

    /**
     * Create an empty page response.
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), page, size, 0, 0, true, true);
    }
}
