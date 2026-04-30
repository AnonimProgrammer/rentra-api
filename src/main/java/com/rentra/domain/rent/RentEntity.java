package com.rentra.domain.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.rentra.domain.BaseEntity;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rents")
@Getter
@Setter
public class RentEntity extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rental_agency_id", nullable = false)
    private RentalAgencyEntity rentalAgency;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal totalAmount;

    @Column(name = "rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    private RentStatus status;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
