package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleStatus;

import java.util.List;
import java.util.UUID;

public record VehicleDetailsResponse(
        UUID id,
        UUID rentalServiceId,
        String rentalServiceName,
        VehicleCategory category,
        String brand,
        String model,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount,
        VehicleStatus status,
        List<VehicleRateResponse> rates,
        Boolean available
) {
}
