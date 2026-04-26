package com.rentra.dto.vehicle;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID vehicleId
) {
}
