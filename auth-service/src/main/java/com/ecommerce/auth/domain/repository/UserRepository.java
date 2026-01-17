package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.model.User;
import com.ecommerce.auth.domain.model.UserId;

import java.util.Optional;

/**
 * Repository port interface for User aggregate.
 * 
 * This is a domain interface (port), implementation is in infrastructure layer.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteById(UserId id);
}
