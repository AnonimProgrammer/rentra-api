package com.rentra.domain.rental_agency;

import java.util.UUID;

import com.rentra.domain.BaseEntity;
import com.rentra.domain.user.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rental_agency_users")
@Getter
@Setter
public class RentalAgencyUserEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "rental_agency_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID rentalAgencyId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "TEXT")
    private AgencyRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    private UserStatus status;
}
