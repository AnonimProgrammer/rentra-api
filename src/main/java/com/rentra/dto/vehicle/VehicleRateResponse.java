package com.rentra.dto.vehicle;

import com.rentra.domain.vehicle.Currency;
import com.rentra.domain.vehicle.RateType;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleRateResponse(
        UUID id,
        RateType rateType,
        BigDecimal price,
        Currency currency
) {
}
