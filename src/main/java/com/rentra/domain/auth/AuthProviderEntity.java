package com.rentra.domain.auth;

import java.util.UUID;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auth_providers")
@Data
public class AuthProviderEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id = UlidCreator.getUlid().toUuid();

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "TEXT")
    private AuthProviderType type;
}
