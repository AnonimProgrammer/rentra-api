package com.rentra.dto.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.rent.RentStatus;

public record RentResponse(
        UUID id,
        UUID customerId,
        UUID vehicleId,
        UUID rentalAgencyId,
        BigDecimal totalAmount,
        Integer rating,
        RentStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime completedAt) {}
