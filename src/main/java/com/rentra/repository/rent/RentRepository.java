package com.rentra.repository.rent;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;

public interface RentRepository extends JpaRepository<RentEntity, UUID> {
    Optional<RentEntity> findByCustomerIdAndStatus(UUID customerId, RentStatus status);

    boolean existsByCustomerIdAndStatus(UUID customerId, RentStatus status);
}
