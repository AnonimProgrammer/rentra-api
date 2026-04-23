package com.rentra.repository.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.auth.RoleEntity;
import com.rentra.domain.auth.RoleName;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(RoleName name);
}
