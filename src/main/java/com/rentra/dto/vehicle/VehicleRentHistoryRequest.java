package com.rentra.dto.vehicle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.rent.RentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record VehicleRentHistoryRequest(
        UUID cursor,
        @Min(value = 1, message = "Limit must be at least 1") Integer limit,
        RentStatus status,
        OffsetDateTime startedFrom,
        OffsetDateTime startedTo,
        OffsetDateTime completedFrom,
        OffsetDateTime completedTo,
        @DecimalMin(value = "0.0", message = "Minimum total amount must be non-negative") BigDecimal minTotalAmount,
        @DecimalMin(value = "0.0", message = "Maximum total amount must be non-negative") BigDecimal maxTotalAmount,
        @Min(value = 1, message = "Minimum rating must be at least 1")
                @Max(value = 5, message = "Minimum rating must be at most 5")
                Integer minRating,
        @Min(value = 1, message = "Maximum rating must be at least 1")
                @Max(value = 5, message = "Maximum rating must be at most 5")
                Integer maxRating) {}
