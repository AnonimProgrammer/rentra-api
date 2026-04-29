package com.rentra.dto.vehicle;

import java.util.UUID;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record VehicleSearchRequest(
        UUID agencyId,
        VehicleCategory category,
        @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters") String brand,
        @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters") String model,
        TransmissionType transmission,
        FuelType fuelType,
        @Min(value = 1, message = "Seat count must be at least 1")
                @Max(value = 100, message = "Seat count must be at most 100")
                Integer seatCount,
        VehicleStatus status,
        UUID cursor,
        @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit must be at most 100")
                Integer limit) {}
