package com.ecommerce.common.domain;

/**
 * Marker interface for Value Objects.
 * Value Objects are immutable and compared by their values, not identity.
 * 
 * Uses Java 21 pattern: Value Objects should be implemented as records.
 * 
 * Example:
 * {@code
 * public record Money(BigDecimal amount, Currency currency) implements ValueObject {
 *     public Money {
 *         Objects.requireNonNull(amount, "amount must not be null");
 *         Objects.requireNonNull(currency, "currency must not be null");
 *         if (amount.compareTo(BigDecimal.ZERO) < 0) {
 *             throw new IllegalArgumentException("amount must be non-negative");
 * }
 * }
 * }
 * }
 */
public interface ValueObject {
    // Marker interface - records provide equals, hashCode, toString automatically
}
