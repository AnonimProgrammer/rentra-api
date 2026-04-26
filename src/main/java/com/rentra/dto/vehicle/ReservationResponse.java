package com.rentra.dto.vehicle;

import java.util.UUID;

import com.rentra.domain.vehicle.VehicleStatus;

public record ReservationResponse(UUID vehicleId, VehicleStatus vehicleStatus) {}
