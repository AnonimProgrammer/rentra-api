package com.rentra.repository.rental_agency;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
