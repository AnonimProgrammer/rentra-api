package com.rentra.repository.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.auth.AuthProviderEntity;
import com.rentra.domain.auth.AuthProviderType;

public interface AuthProviderRepository extends JpaRepository<AuthProviderEntity, UUID> {
    Optional<AuthProviderEntity> findByType(AuthProviderType type);
}
