package com.ecommerce.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * Utility for generating unique identifiers.
 * Uses ULID (Universally Unique Lexicographically Sortable Identifier).
 * 
 * Benefits over UUID:
 * - Lexicographically sortable (time-ordered)
 * - Shorter string representation
 * - Better for database indexing
 */
public final class IdGenerator {

    private IdGenerator() {
        // Utility class
    }

    /**
     * Generate a new ULID as string.
     */
    public static String generate() {
        return UlidCreator.getUlid().toString();
    }

    /**
     * Generate a new ULID with a prefix.
     * Example: "ord_01HQ3ZYXW9ABCDEF123456"
     */
    public static String generate(String prefix) {
        return prefix + "_" + UlidCreator.getUlid().toString();
    }
}
