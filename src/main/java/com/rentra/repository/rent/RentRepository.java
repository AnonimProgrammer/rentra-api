package com.rentra.repository.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;

public interface RentRepository extends JpaRepository<RentEntity, UUID> {
    List<RentEntity> findByStatus(RentStatus status);

    Optional<RentEntity> findFirstByCustomerIdAndStatusOrderByIdDesc(UUID customerId, RentStatus status);

    boolean existsByCustomerIdAndStatus(UUID customerId, RentStatus status);

    @Query(
            value =
                    """
        SELECT * FROM rents
        WHERE customer_id = :userId
        AND (:status IS NULL OR status = :status)
        AND (:cursorId IS NULL OR id < :cursorId)
        ORDER BY id DESC
        LIMIT :limit
    """,
            nativeQuery = true)
    List<RentEntity> findRentHistory(
            @Param("userId") UUID userId,
            @Param("status") String status,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);

    @Query(
            value =
                    """
        SELECT * FROM rents
        WHERE vehicle_id = :vehicleId
        AND (:status IS NULL OR status = :status)
        AND (:startedFrom IS NULL OR starts_at >= :startedFrom)
        AND (:startedTo IS NULL OR starts_at <= :startedTo)
        AND (:completedFrom IS NULL OR completed_at >= :completedFrom)
        AND (:completedTo IS NULL OR completed_at <= :completedTo)
        AND (:minTotalAmount IS NULL OR total_amount >= :minTotalAmount)
        AND (:maxTotalAmount IS NULL OR total_amount <= :maxTotalAmount)
        AND (:minRating IS NULL OR rating >= :minRating)
        AND (:maxRating IS NULL OR rating <= :maxRating)
        AND (:cursorId IS NULL OR id < :cursorId)
        ORDER BY id DESC
        LIMIT :limit
    """,
            nativeQuery = true)
    List<RentEntity> findVehicleRentHistory(
            @Param("vehicleId") UUID vehicleId,
            @Param("status") String status,
            @Param("startedFrom") OffsetDateTime startedFrom,
            @Param("startedTo") OffsetDateTime startedTo,
            @Param("completedFrom") OffsetDateTime completedFrom,
            @Param("completedTo") OffsetDateTime completedTo,
            @Param("minTotalAmount") BigDecimal minTotalAmount,
            @Param("maxTotalAmount") BigDecimal maxTotalAmount,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
