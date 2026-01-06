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
        super(ErrorCode.VALIDATION_ERROR.getCode(), message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR.getCode(), message);
        this.fieldErrors = fieldErrors != null ? Map.copyOf(fieldErrors) : Collections.emptyMap();
    }

    public ValidationException(String field, String error) {
        super(ErrorCode.VALIDATION_ERROR.getCode(),
                String.format("Validation failed for field '%s': %s", field, error));
        this.fieldErrors = Map.of(field, List.of(error));
    }

    public ValidationException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
        this.fieldErrors = Collections.emptyMap();
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
