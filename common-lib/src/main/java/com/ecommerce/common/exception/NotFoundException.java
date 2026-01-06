package com.ecommerce.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public final class NotFoundException extends BusinessException {

    private final String resourceType;
    private final String resourceId;

    public NotFoundException(String resourceType, String resourceId) {
        super(ErrorCode.NOT_FOUND, resourceType, resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(String resourceType, String resourceId, Throwable cause) {
        super(ErrorCode.NOT_FOUND, cause, resourceType, resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
        this.resourceType = "Unknown";
        this.resourceId = "Unknown";
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
