package com.rentra.repository.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentra.domain.vehicle.*;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    @Query(
            """
SELECT v FROM Vehicle v
WHERE v.status = 'AVAILABLE'
AND (:category IS NULL OR v.category = :category)
AND (:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
AND (:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))
AND (:transmission IS NULL OR v.transmission = :transmission)
AND (:fuelType IS NULL OR v.fuelType = :fuelType)
AND (:seatCount IS NULL OR v.seatCount = :seatCount)
""")
    List<Vehicle> searchAvailableVehicles(
            @Param("category") VehicleCategory category,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("transmission") TransmissionType transmission,
            @Param("fuelType") FuelType fuelType,
            @Param("seatCount") Integer seatCount);
}
