package com.rentra.service.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.user.UserEntity;
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
        vehicle.setStatus(VehicleStatus.TECHNICAL_CHECK);
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

    public RentEntity create(UserEntity customer, VehicleEntity vehicle) {
        RentEntity rent = new RentEntity();
        rent.setCustomer(customer);
        rent.setVehicle(vehicle);
        rent.setStatus(RentStatus.ACTIVE);
        rent.setTotalAmount(BigDecimal.ZERO);
        rent.setStartsAt(OffsetDateTime.now());

        return rentRepository.save(rent);
    }

    public List<RentResponse> getActive() {
        List<RentEntity> rents = rentRepository.findByStatus(RentStatus.ACTIVE);

        return rents.stream().map(rentMapper::toResponse).toList();
    }

    public RentEntity findOrThrow(UUID rentId) {
        return rentRepository
                .findById(rentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent not found for id: " + rentId));
    }
}
