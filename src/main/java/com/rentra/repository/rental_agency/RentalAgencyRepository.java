package com.rentra.repository.rental_agency;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rentra.domain.rental_agency.RentalAgencyEntity;

@Repository
public interface RentalAgencyRepository extends JpaRepository<RentalAgencyEntity, UUID> {}
