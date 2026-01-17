package com.ecommerce.auth.domain.model;

/**
 * User roles for authorization.
 * 
 * - ADMIN: Full system access, can manage all resources
 * - CUSTOMER: Regular user, can create orders and view own data
 * - SERVICE: Internal service-to-service communication
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public enum Role {
    ADMIN,
    CUSTOMER,
    SERVICE
}
