package com.rentra.dto.rental_service;

import java.math.BigDecimal;
import java.util.UUID;

import com.rentra.domain.rental_service.RentalServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RentalServiceRequest {

    @NotNull
    private UUID ownerId;

    @NotBlank
    private String name;

    private String description;

    private RentalServiceStatus status;

    @NotNull
    private BigDecimal locationLat;

    @NotNull
    private BigDecimal locationLng;

    public RentalServiceRequest() {}

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }

    public BigDecimal getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(BigDecimal locationLng) {
        this.locationLng = locationLng;
    }

    public RentalServiceStatus getStatus() {
        return status;
    }

    public void setStatus(RentalServiceStatus status) {
        this.status = status;
    }
}
