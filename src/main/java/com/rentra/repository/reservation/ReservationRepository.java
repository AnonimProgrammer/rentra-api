package com.rentra.repository.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentra.domain.reservation.ReservationEntity;
import com.rentra.domain.reservation.ReservationStatus;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    boolean existsByCustomerIdAndStatus(UUID customerId, ReservationStatus status);

    @Query(
            value =
                    """
            SELECT * FROM reservations
            WHERE rental_agency_id = :agencyId
              AND (:vehicleId IS NULL OR vehicle_id = :vehicleId)
              AND (:customerId IS NULL OR customer_id = :customerId)
              AND (:status IS NULL OR status = :status)
              AND (:cursorId IS NULL OR id < :cursorId)
            ORDER BY id DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ReservationEntity> findAgencyReservations(
            @Param("agencyId") UUID agencyId,
            @Param("vehicleId") UUID vehicleId,
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
