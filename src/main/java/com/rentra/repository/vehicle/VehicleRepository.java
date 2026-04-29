package com.rentra.repository.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentra.domain.vehicle.VehicleEntity;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    @Query(
            value =
                    """
            SELECT * FROM vehicles
            WHERE (:agencyId IS NULL OR rental_agency_id = :agencyId)
              AND (:category IS NULL OR category = :category)
              AND (:brand IS NULL OR LOWER(brand) LIKE LOWER(CONCAT('%', :brand, '%')))
              AND (:model IS NULL OR LOWER(model) LIKE LOWER(CONCAT('%', :model, '%')))
              AND (:transmission IS NULL OR transmission = :transmission)
              AND (:fuelType IS NULL OR fuel_type = :fuelType)
              AND (:seatCount IS NULL OR seat_count = :seatCount)
              AND (:status IS NULL OR status = :status)
              AND (:cursorId IS NULL OR id < :cursorId)
            ORDER BY id DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<VehicleEntity> findVehicles(
            @Param("agencyId") UUID agencyId,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("transmission") String transmission,
            @Param("fuelType") String fuelType,
            @Param("seatCount") Integer seatCount,
            @Param("status") String status,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
