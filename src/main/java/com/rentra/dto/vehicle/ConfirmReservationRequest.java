package com.rentra.dto.vehicle;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ConfirmReservationRequest(@NotNull(message = "Customer id is required") UUID customerId) {}
