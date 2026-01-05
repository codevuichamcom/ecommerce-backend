package com.ecommerce.common.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public final class ValidationException extends BusinessException {

    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors != null ? Map.copyOf(fieldErrors) : Collections.emptyMap();
    }

    public ValidationException(String field, String error) {
        super("VALIDATION_ERROR", String.format("Validation failed for field '%s': %s", field, error));
        this.fieldErrors = Map.of(field, List.of(error));
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
