package com.rentra.dto.vehicle;

import java.math.BigDecimal;
import java.util.UUID;

import com.rentra.domain.vehicle.Currency;
import com.rentra.domain.vehicle.RateType;

public record VehicleRateResponse(UUID id, RateType rateType, BigDecimal price, Currency currency) {}
