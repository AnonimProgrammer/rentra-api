package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.VehicleStatus;

import java.util.UUID;

public record ReservationResponse(
        UUID vehicleId,
        VehicleStatus vehicleStatus
) {
}
