package com.rentra.repository.reservation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.reservation.ReservationEntity;
import com.rentra.domain.reservation.ReservationStatus;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    boolean existsByCustomerIdAndStatus(UUID customerId, ReservationStatus status);
}
