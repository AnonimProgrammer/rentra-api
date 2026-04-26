package com.rentra.domain.auth;

import java.util.Map;

import com.rentra.domain.BaseEntity;
import com.rentra.domain.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_auth")
@Getter
@Setter
public class UserAuthEntity extends BaseEntity {
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
}
