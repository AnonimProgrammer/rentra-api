package com.rentra.dto.vehicle;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(@NotNull UUID vehicleId) {}
