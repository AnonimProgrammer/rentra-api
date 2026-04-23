package com.rentra.repository.user;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.user.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {}
