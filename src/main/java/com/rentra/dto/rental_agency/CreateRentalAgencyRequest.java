package com.rentra.dto.rental_agency;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record CreateRentalAgencyRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        BigDecimal locationLat,
        BigDecimal locationLng) {}
