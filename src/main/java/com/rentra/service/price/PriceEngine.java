package com.rentra.service.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.vehicle.RateType;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.exception.UnsupportedOperationException;

@Service
public class PriceEngine {
    public BigDecimal calculateFinalAmount(RentEntity rent) {
        OffsetDateTime completedAt = rent.getCompletedAt();
        if (completedAt == null) {
            throw new IllegalArgumentException("Rent completion time is required to calculate final amount");
        }

        Duration duration = Duration.between(rent.getStartsAt(), completedAt);
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Rent completion time can not be before start time");
        }

        long billedUnits = Math.max(1L, duration.toHours());
        RateType rateType;

        if (duration.toHours() >= 24) {
            billedUnits = Math.max(1L, (long) Math.ceil(duration.toHours() / 24d));
            rateType = RateType.DAY;
        } else {
            rateType = RateType.HOUR;
        }

        VehicleRateEntity rate = rent.getVehicle().getRates().stream()
                .filter(item -> item.getType() == rateType)
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No " + rateType + " rate configured"));

        return rate.getPrice().multiply(BigDecimal.valueOf(billedUnits)).setScale(2, RoundingMode.HALF_UP);
    }
}
