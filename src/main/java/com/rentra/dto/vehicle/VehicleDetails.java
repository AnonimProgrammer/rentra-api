package com.rentra.dto.vehicle;

import java.util.List;
import java.util.UUID;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.rental_agency.RentalAgencySummary;

public record VehicleDetails(
        UUID id,
        RentalAgencySummary rentalAgency,
        VehicleCategory category,
        String brand,
        String model,
        TransmissionType transmission,
        FuelType fuelType,
        Integer seatCount,
        VehicleStatus status,
        List<VehicleRateResponse> rates) {}
