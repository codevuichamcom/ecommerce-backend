package com.ecommerce.auth.infrastructure.seed.factory;

import com.ecommerce.auth.domain.model.User;

import java.util.List;

/**
 * Container for seeded data returned by the factory.
 */
public record SeededData(
        List<User> users,
        int enabledCount,
        int disabledCount) {
    public int totalCount() {
        return users.size();
    }
}
