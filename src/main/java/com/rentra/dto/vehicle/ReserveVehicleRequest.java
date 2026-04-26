package com.rentra.dto.vehicle;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReserveVehicleRequest(@NotNull(message = "Vehicle id is required") UUID vehicleId) {}
