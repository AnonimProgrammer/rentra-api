package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record VehicleSearchRequest(
        VehicleCategory category,
        @Size(min = 2, max = 50) String brand,
        @Size(min = 1, max = 50) String model,
        TransmissionType transmission,
        FuelType fuelType,
        @Min(1) @Max(100) Integer seatCount,
        VehicleStatus status) {}
