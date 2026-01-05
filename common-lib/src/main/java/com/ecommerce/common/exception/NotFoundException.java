package com.ecommerce.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public final class NotFoundException extends BusinessException {

    private final String resourceType;
    private final String resourceId;

    public NotFoundException(String resourceType, String resourceId) {
        super("NOT_FOUND", String.format("%s with id '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(String resourceType, String resourceId, Throwable cause) {
        super("NOT_FOUND", String.format("%s with id '%s' not found", resourceType, resourceId), cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
