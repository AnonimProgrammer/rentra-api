package com.rentra.domain.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.rentra.domain.BaseEntity;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class VehicleEntity extends BaseEntity implements Persistable<UUID> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "rental_agency_id", nullable = false)
    private RentalAgencyEntity rentalAgency;

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

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleRateEntity> rates = new ArrayList<>();

    @Transient
    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
