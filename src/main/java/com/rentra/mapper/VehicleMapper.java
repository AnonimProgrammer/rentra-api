package com.rentra.mapper;

import com.rentra.domain.vehicle.Vehicle;
import com.rentra.domain.vehicle.VehicleRate;
import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleRateResponse;
import com.rentra.dto.vehicle.VehicleSummaryResponse;

public final class VehicleMapper {

    private VehicleMapper() {}

    public static VehicleSummaryResponse toSummaryResponse(Vehicle vehicle) {
        return new VehicleSummaryResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getCategory(),
                vehicle.getTransmission(),
                vehicle.getFuelType(),
                vehicle.getSeatCount(),
                vehicle.getStatus());
    }

    private static VehicleRateResponse toRateResponse(VehicleRate rate) {
        return new VehicleRateResponse(rate.getId(), rate.getType(), rate.getPrice(), rate.getCurrency());
    }

    public static VehicleDetailsResponse toDetailsResponse(Vehicle vehicle, Boolean available) {

        return new VehicleDetailsResponse(
                vehicle.getId(),
                vehicle.getRentalService().getId(),
                vehicle.getRentalService().getName(),
                vehicle.getCategory(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getTransmission(),
                vehicle.getFuelType(),
                vehicle.getSeatCount(),
                vehicle.getStatus(),
                vehicle.getRates().stream().map(VehicleMapper::toRateResponse).toList(),
                available);
    }
}
