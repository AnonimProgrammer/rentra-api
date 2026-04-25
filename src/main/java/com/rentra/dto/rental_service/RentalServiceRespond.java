package com.rentra.dto.rental_service;

import java.math.BigDecimal;
import java.util.UUID;

public class RentalServiceRespond {
    private UUID id;
    private String name;
    private String description;
    private String owner;
    private BigDecimal locationLat;
    private BigDecimal locationLng;

    public RentalServiceRespond() {}

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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
