package com.rentra.dto.reservation;

import java.util.UUID;

import com.rentra.domain.reservation.ReservationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReservationSearchRequest(
        UUID vehicleId,
        UUID customerId,
        ReservationStatus status,
        UUID cursor,
        @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit must be at most 100")
                Integer limit) {}
