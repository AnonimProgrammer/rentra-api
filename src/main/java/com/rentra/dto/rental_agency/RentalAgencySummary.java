package com.rentra.dto.rental_agency;

import java.math.BigDecimal;
import java.util.UUID;

public record RentalAgencySummary(
        UUID id, String name, String description, BigDecimal locationLat, BigDecimal locationLng) {}
