package com.rentra.service.rent;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.rent.RentResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.price.PriceEngine;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentService {
    private final RentRepository rentRepository;
    private final VehicleRepository vehicleRepository;
    private final PriceEngine priceEngine;
    private final RentMapper rentMapper;

    @Transactional
    public RentResponse complete(UUID rentId) {
        RentEntity rent = findOrThrow(rentId);
        if (rent.getStatus() != RentStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE rent can be completed");
        }

        rent.setCompletedAt(OffsetDateTime.now());
        rent.setStatus(RentStatus.COMPLETED);
        rent.setTotalAmount(priceEngine.calculateFinalAmount(rent));

        VehicleEntity vehicle = rent.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        return rentMapper.toResponse(rentRepository.save(rent));
    }

    @Transactional
    public RentResponse rate(UUID rentId, Integer rating) {
        RentEntity rent = findOrThrow(rentId);
        if (rent.getStatus() != RentStatus.COMPLETED) {
            throw new IllegalArgumentException("Rating is only allowed for COMPLETED rent");
        }

        rent.setRating(rating);
        return rentMapper.toResponse(rentRepository.save(rent));
    }

    public RentEntity findOrThrow(UUID rentId) {
        return rentRepository
                .findById(rentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent not found for id: " + rentId));
    }
}
