package com.rentra.repository.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.UserAuthEntity;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, UUID> {
    @Query(
            """
      select ua
      from UserAuthEntity ua
      join ua.provider p
      where p.type = :provider and ua.providerUserId = :providerUserId
      """)
    Optional<UserAuthEntity> findByProviderTypeAndProviderUserId(
            @Param("provider") AuthProviderType provider, @Param("providerUserId") String providerUserId);

    @Query(
            """
      select ua
      from UserAuthEntity ua
      join ua.provider p
      where p.type = :provider and ua.email = :email
      """)
    Optional<UserAuthEntity> findByProviderTypeAndEmail(
            @Param("provider") AuthProviderType provider, @Param("email") String email);
}
