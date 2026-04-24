package com.rentra.repository;

import com.rentra.domain.vehicle.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByRentalServiceId(UUID serviceId);
    List<Vehicle> findByStatus(VehicleStatus status);
    @Query("""
    SELECT v FROM Vehicle v
    WHERE (:category IS NULL OR v.category = :category)
    AND (:brand IS NULL OR v.brand = :brand)
    AND (:transmission IS NULL OR v.transmission = :transmission)
    AND (:fuelType IS NULL OR v.fuelType = :fuelType)
    """)
    List<Vehicle> searchVehicles(
            @Param("category") VehicleCategory category,
            @Param("brand") String brand,
            @Param("transmission") TransmissionType transmission,
            @Param("fuelType") FuelType fuelType
    );
}
