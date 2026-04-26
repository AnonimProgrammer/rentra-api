package com.rentra.mapper;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleRateResponse;
import com.rentra.dto.vehicle.VehicleSummaryResponse;

public final class VehicleMapper {

    private VehicleMapper() {}

    public static VehicleEntity toEntity(CreateVehicleRequest request, RentalAgencyEntity rentalAgency) {
        VehicleEntity vehicleEntity = new VehicleEntity();

        vehicleEntity.setRentalAgency(rentalAgency);
        vehicleEntity.setCategory(request.category());
        vehicleEntity.setBrand(request.brand());
        vehicleEntity.setModel(request.model());
        vehicleEntity.setTransmission(request.transmission());
        vehicleEntity.setFuelType(request.fuelType());
        vehicleEntity.setSeatCount(request.seatCount());
        vehicleEntity.setStatus(VehicleStatus.AVAILABLE);
        return vehicleEntity;
    }

    public static VehicleSummaryResponse toSummaryResponse(VehicleEntity vehicleEntity) {
        return new VehicleSummaryResponse(
                vehicleEntity.getId(),
                vehicleEntity.getBrand(),
                vehicleEntity.getModel(),
                vehicleEntity.getCategory(),
                vehicleEntity.getTransmission(),
                vehicleEntity.getFuelType(),
                vehicleEntity.getSeatCount(),
                vehicleEntity.getStatus());
    }

    private static VehicleRateResponse toRateResponse(VehicleRateEntity rate) {
        return new VehicleRateResponse(rate.getId(), rate.getType(), rate.getPrice(), rate.getCurrency());
    }

    public static VehicleDetailsResponse toDetailsResponse(VehicleEntity vehicleEntity, Boolean available) {

        return new VehicleDetailsResponse(
                vehicleEntity.getId(),
                vehicleEntity.getRentalAgency().getId(),
                vehicleEntity.getRentalAgency().getName(),
                vehicleEntity.getCategory(),
                vehicleEntity.getBrand(),
                vehicleEntity.getModel(),
                vehicleEntity.getTransmission(),
                vehicleEntity.getFuelType(),
                vehicleEntity.getSeatCount(),
                vehicleEntity.getStatus(),
                vehicleEntity.getRates().stream()
                        .map(VehicleMapper::toRateResponse)
                        .toList(),
                available);
    }
}
