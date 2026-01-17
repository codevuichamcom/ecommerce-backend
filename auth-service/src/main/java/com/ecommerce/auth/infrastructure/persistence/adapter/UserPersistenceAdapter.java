package com.ecommerce.auth.infrastructure.persistence.adapter;

import com.ecommerce.auth.domain.model.User;
import com.ecommerce.auth.domain.model.UserId;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter for User.
 * Maps between domain model and JPA entity.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserPersistenceAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    @SuppressWarnings("null")
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<User> findById(UserId id) {
        return jpaUserRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    @SuppressWarnings("null")
    public void deleteById(UserId id) {
        jpaUserRepository.deleteById(id.value());
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity(
                user.getId().value(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles());
        entity.setEnabled(user.isEnabled());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        return entity;
    }

    private User toDomain(UserEntity entity) {
        return new User(
                new UserId(entity.getId()),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRoles(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getLastLoginAt());
    }
}
