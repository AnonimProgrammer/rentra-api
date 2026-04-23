package com.rentra.repository.auth;

import com.rentra.domain.auth.AuthProviderEntity;
import com.rentra.domain.auth.AuthProviderType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthProviderRepository extends JpaRepository<AuthProviderEntity, UUID> {
  Optional<AuthProviderEntity> findByType(AuthProviderType type);
}
