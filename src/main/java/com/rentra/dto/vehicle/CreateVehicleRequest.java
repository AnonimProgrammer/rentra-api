package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;

import java.util.UUID;

public record CreateVehicleRequest(
        UUID rentalServiceId,
        VehicleCategory category,
        String brand,
        String model,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount
) {
}
