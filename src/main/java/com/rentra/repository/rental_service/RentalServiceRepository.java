package com.rentra.repository.rental_service;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rentra.domain.rental_service.RentalServiceEntity;

@Repository
public interface RentalServiceRepository extends JpaRepository<RentalServiceEntity, UUID> {}
