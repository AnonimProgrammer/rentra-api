package com.rentra.service.price;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.rentra.domain.payment.Currency;
import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.vehicle.RateType;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.exception.UnsupportedOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceEngineTest {
    private final PriceEngine priceEngine = new PriceEngine();

    // =========================== calculateFinalAmount ===========================

    @Test
    void calculateFinalAmount_shouldUseHourlyRate_whenDurationLessThan24Hours() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T15:00:00Z"),
                List.of(createRate(RateType.HOUR, "12.50")));

        BigDecimal result = priceEngine.calculateFinalAmount(rent);

        assertEquals(new BigDecimal("62.50"), result);
    }

    @Test
    void calculateFinalAmount_shouldUseMinimumOneHourCharge_whenDurationIsZero() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                List.of(createRate(RateType.HOUR, "12.50")));

        BigDecimal result = priceEngine.calculateFinalAmount(rent);

        assertEquals(new BigDecimal("12.50"), result);
    }

    @Test
    void calculateFinalAmount_shouldUseDailyRateWithCeiling_whenDurationAtLeast24Hours() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-03T11:00:00Z"),
                List.of(createRate(RateType.DAY, "50.00")));

        BigDecimal result = priceEngine.calculateFinalAmount(rent);

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void calculateFinalAmount_shouldThrowIllegalArgumentException_whenCompletedAtIsNull() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-01T10:00:00Z"), null, List.of(createRate(RateType.HOUR, "12.50")));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> priceEngine.calculateFinalAmount(rent));

        assertEquals("Rent completion time is required to calculate final amount", exception.getMessage());
    }

    @Test
    void calculateFinalAmount_shouldThrowIllegalArgumentException_whenCompletedBeforeStart() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-02T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                List.of(createRate(RateType.HOUR, "12.50")));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> priceEngine.calculateFinalAmount(rent));

        assertEquals("Rent completion time can not be before start time", exception.getMessage());
    }

    @Test
    void calculateFinalAmount_shouldThrowUnsupportedOperationException_whenRequiredRateMissing() {
        RentEntity rent = createRent(
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T15:00:00Z"),
                List.of(createRate(RateType.DAY, "50.00")));

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class, () -> priceEngine.calculateFinalAmount(rent));

        assertEquals("No HOUR rate configured", exception.getMessage());
    }

    private RentEntity createRent(OffsetDateTime startsAt, OffsetDateTime completedAt, List<VehicleRateEntity> rates) {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(UUID.randomUUID());
        vehicle.setRates(rates);

        RentEntity rent = new RentEntity();
        rent.setVehicle(vehicle);
        rent.setStartsAt(startsAt);
        rent.setCompletedAt(completedAt);
        return rent;
    }

    private VehicleRateEntity createRate(RateType rateType, String price) {
        VehicleRateEntity rate = new VehicleRateEntity();
        rate.setType(rateType);
        rate.setPrice(new BigDecimal(price));
        rate.setCurrency(Currency.AZN);
        return rate;
    }
}
