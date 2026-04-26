package com.rentra.dto.vehicle;

import java.util.UUID;

import com.rentra.domain.vehicle.*;

public record VehicleSummary(
        UUID id,
        String brand,
        String model,
        VehicleCategory category,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount,
        VehicleStatus status) {}
