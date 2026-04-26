package com.rentra.dto.vehicle;

import java.util.UUID;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;

public record CreateVehicleRequest(
        UUID rentalAgencyId,
        VehicleCategory category,
        String brand,
        String model,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount) {}
