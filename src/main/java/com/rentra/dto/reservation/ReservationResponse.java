package com.rentra.dto.reservation;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.reservation.ReservationStatus;

public record ReservationResponse(
        UUID id,
        UUID customerId,
        UUID vehicleId,
        UUID rentalAgencyId,
        ReservationStatus status,
        OffsetDateTime reservedAt,
        OffsetDateTime confirmedAt,
        OffsetDateTime cancelledAt,
        OffsetDateTime expiresAt) {}
