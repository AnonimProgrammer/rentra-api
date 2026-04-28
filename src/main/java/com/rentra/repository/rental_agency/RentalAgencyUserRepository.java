package com.rentra.repository.rental_agency;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyUserEntity;
import com.rentra.domain.user.UserStatus;

@Repository
public interface RentalAgencyUserRepository extends JpaRepository<RentalAgencyUserEntity, UUID> {
    @Query(
            """
            select rau
            from RentalAgencyUserEntity rau
            where rau.userId = :userId
              and rau.rentalAgencyId = :rentalAgencyId
            """)
    Optional<RentalAgencyUserEntity> findMembership(UUID userId, UUID rentalAgencyId);

    @Query(
            """
            select count(rau) > 0
            from RentalAgencyUserEntity rau
            where rau.userId = :userId
              and rau.rentalAgencyId = :rentalAgencyId
              and rau.status = :status
              and rau.role in :roles
            """)
    boolean hasAuthorization(UUID userId, UUID rentalAgencyId, UserStatus status, List<AgencyRole> roles);

    @Query(
            value =
                    """
            SELECT * FROM rental_agency_users
            WHERE rental_agency_id = :agencyId
              AND (:role IS NULL OR role = :role)
              AND (:status IS NULL OR status = :status)
              AND (:cursorId IS NULL OR user_id < :cursorId)
            ORDER BY user_id DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<RentalAgencyUserEntity> findAgencyMemberships(
            @Param("agencyId") UUID agencyId,
            @Param("role") String role,
            @Param("status") String status,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
