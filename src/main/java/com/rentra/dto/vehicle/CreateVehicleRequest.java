package com.rentra.dto.vehicle;

import java.util.UUID;

import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
        @NotNull(message = "Rental agency id is required") UUID rentalAgencyId,
        @NotNull(message = "Category is required") VehicleCategory category,
        @NotBlank(message = "Brand is required")
                @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
                String brand,
        @NotBlank(message = "Model is required")
                @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
                String model,
        @NotNull(message = "Transmission is required") TransmissionType transmission,
        @NotNull(message = "Fuel type is required") FuelType fuelType,
        @NotNull(message = "Seat count is required")
                @Min(value = 1, message = "Seat count must be at least 1")
                @Max(value = 100, message = "Seat count must be at most 100")
                Integer seatCount) {}
