package com.rentra.domain.vehicle;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rentra.domain.rental_service.RentalServiceEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vehicles")
@Data
public class VehicleEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id = UlidCreator.getUlid().toUuid();

    @ManyToOne(optional = false)
    @JoinColumn(name = "rental_service_id", nullable = false)
    private RentalServiceEntity rentalService;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "TEXT")
    private VehicleCategory category;

    @Column(name = "brand", nullable = false, columnDefinition = "TEXT")
    private String brand;

    @Column(name = "model", nullable = false, columnDefinition = "TEXT")
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, columnDefinition = "TEXT")
    private TransmissionType transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, columnDefinition = "TEXT")
    private FuelType fuelType;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @OneToMany(mappedBy = "vehicle")
    private List<VehicleRateEntity> rates = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
