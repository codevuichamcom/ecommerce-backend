package com.ecommerce.common.exception;

/**
 * Exception thrown when there's a conflict (e.g., duplicate resource,
 * optimistic lock failure).
 */
public final class ConflictException extends BusinessException {

    private final String conflictType;

    public ConflictException(String conflictType, String message) {
        super("CONFLICT", message);
        this.conflictType = conflictType;
    }

    public ConflictException(String conflictType, String message, Throwable cause) {
        super("CONFLICT", message, cause);
        this.conflictType = conflictType;
    }

    /**
     * Create exception for duplicate resource.
     */
    public static ConflictException duplicate(String resourceType, String identifier) {
        return new ConflictException(
                "DUPLICATE",
                String.format("%s with identifier '%s' already exists", resourceType, identifier));
    }

    /**
     * Create exception for optimistic lock failure.
     */
    public static ConflictException concurrentModification(String resourceType, String resourceId) {
        return new ConflictException(
                "CONCURRENT_MODIFICATION",
                String.format("%s '%s' was modified by another transaction", resourceType, resourceId));
    }

    public String getConflictType() {
        return conflictType;
    }
}
