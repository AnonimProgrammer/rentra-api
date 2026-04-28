package com.rentra.repository.rent;

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
}
