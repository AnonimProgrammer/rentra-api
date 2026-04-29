package com.rentra.dto.vehicle;

import java.math.BigDecimal;

import com.rentra.domain.payment.Currency;
import com.rentra.domain.vehicle.RateType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateVehicleRateRequest(
        @NotNull(message = "Rate type is required") RateType rateType,
        @NotNull(message = "Price is required")
                @DecimalMin(value = "0.01", message = "Price must be greater than 0")
                BigDecimal price,
        @NotNull(message = "Currency is required") Currency currency) {}
