package com.rentra.domain.rental_agency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rentra.domain.BaseEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rental_agencies")
@Getter
@Setter
public class RentalAgencyEntity extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity ownerUser;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 7, name = "location_lat", columnDefinition = "DECIMAL(10,7)")
    private BigDecimal locationLat;

    @Column(precision = 10, scale = 7, name = "location_lng", columnDefinition = "DECIMAL(10,7)")
    private BigDecimal locationLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "TEXT")
    private RentalAgencyStatus status = RentalAgencyStatus.ACTIVE;

    @OneToMany(mappedBy = "rentalAgency")
    @JsonIgnore
    private List<VehicleEntity> vehicles = new ArrayList<>();
}
