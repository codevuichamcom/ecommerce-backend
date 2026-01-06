package com.ecommerce.common.exception;

/**
 * Base exception for all business/domain exceptions.
 * Uses Java 21 sealed class for type-safe exception hierarchy.
 * 
 * Pattern matching can be used to handle different exception types:
 * {@code
 * switch (exception) {
 *     case NotFoundException e -> handleNotFound(e);
 *     case ConflictException e -> handleConflict(e);
 *     case ValidationException e -> handleValidation(e);
 * }
 * }
 */
public abstract sealed class BusinessException extends RuntimeException
        permits NotFoundException, ConflictException, ValidationException {

    private final String errorCode;

    protected BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode.getCode();
    }

    protected BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode.getCode();
    }

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
