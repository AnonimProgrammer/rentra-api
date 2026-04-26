package com.rentra.domain.auth;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rentra.domain.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_auth")
@Data
public class UserAuthEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id = UlidCreator.getUlid().toUuid();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private AuthProviderEntity provider;

    @Column(name = "provider_user_id", columnDefinition = "TEXT")
    private String providerUserId;

    @Column(name = "email", columnDefinition = "TEXT")
    private String email;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
