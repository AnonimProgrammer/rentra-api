package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.*;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleSummaryResponse(
        UUID id,
        String brand,
        String model,
        VehicleCategory category,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount,
        VehicleStatus status
) {
}
