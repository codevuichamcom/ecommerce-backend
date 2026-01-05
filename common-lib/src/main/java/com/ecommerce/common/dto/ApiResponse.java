package com.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Standard API response wrapper.
 * Uses Java 21 record for immutability.
 *
 * @param isSuccess Whether the operation succeeded
 * @param data      The response payload
 * @param error     Error details if failed
 * @param timestamp Response timestamp
 * @param <T>       Type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @JsonProperty("success") boolean isSuccess,
        T data,
        ErrorDetail error,
        Instant timestamp) {

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    /**
     * Create a successful response without data.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null, Instant.now());
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, null), Instant.now());
    }

    /**
     * Create an error response with details.
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, details), Instant.now());
    }

    /**
     * Error detail record.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            String code,
            String message,
            Object details) {
    }
}
