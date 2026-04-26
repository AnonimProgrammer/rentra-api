package com.rentra.repository.rent;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentra.domain.rent.RentEntity;

public interface RentRepository extends JpaRepository<RentEntity, UUID> {}
