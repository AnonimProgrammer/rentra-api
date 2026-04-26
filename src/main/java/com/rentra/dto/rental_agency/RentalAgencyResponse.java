package com.rentra.dto.rental_agency;

import java.math.BigDecimal;
import java.util.UUID;

import com.rentra.dto.user.MeResponse;

public record RentalAgencyResponse(
        UUID id, String name, String description, MeResponse owner, BigDecimal locationLat, BigDecimal locationLng) {}
