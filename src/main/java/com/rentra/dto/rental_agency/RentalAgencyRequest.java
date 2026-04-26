package com.rentra.dto.rental_agency;

import java.math.BigDecimal;
import java.util.UUID;

import com.rentra.domain.rental_agency.RentalAgencyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RentalAgencyRequest {

    @NotNull
    private UUID ownerId;

    @NotBlank
    private String name;

    private String description;

    private RentalAgencyStatus status;

    @NotNull
    private BigDecimal locationLat;

    @NotNull
    private BigDecimal locationLng;

    public RentalAgencyRequest() {}

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

    public RentalAgencyStatus getStatus() {
        return status;
    }

    public void setStatus(RentalAgencyStatus status) {
        this.status = status;
    }
}
