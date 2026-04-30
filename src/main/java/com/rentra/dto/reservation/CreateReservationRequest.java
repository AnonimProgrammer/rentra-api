package com.rentra.dto.reservation;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(@NotNull(message = "Vehicle id is required") UUID vehicleId) {}
