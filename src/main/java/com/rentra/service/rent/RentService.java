package com.rentra.service.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
import com.rentra.repository.user.UserRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.price.PriceEngine;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentService {
    private final RentRepository rentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
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

    @Transactional
    public RentResponse confirmReservation(UUID vehicleId, UUID customerId) {
        UserEntity customer =
                userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        if (rentRepository.existsByCustomerIdAndStatus(customer.getId(), RentStatus.ACTIVE)) {
            throw new RuntimeException("Rent is already active");
        }

        VehicleEntity vehicle =
                vehicleRepository.findById(vehicleId).orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (vehicle.getStatus() != VehicleStatus.RESERVED) {
            throw new RuntimeException("Vehicle is already reserved");
        }

        RentEntity rent = new RentEntity();
        rent.setCustomer(customer);
        rent.setVehicle(vehicle);
        rent.setStatus(RentStatus.ACTIVE);
        rent.setTotalAmount(BigDecimal.ZERO);
        rent.setStartsAt(OffsetDateTime.now());
        vehicle.setStatus(VehicleStatus.RENTED);

        RentEntity savedRent = rentRepository.save(rent);
        return rentMapper.toResponse(savedRent);
    }

    public RentResponse getActiveRent(UUID customerId) {
        UserEntity customer =
                userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        RentEntity rent = rentRepository
                .findByCustomerIdAndStatus(customer.getId(), RentStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active rent not found"));
        return rentMapper.toResponse(rent);
    }

    public RentEntity findOrThrow(UUID rentId) {
        return rentRepository
                .findById(rentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent not found for id: " + rentId));
    }
}
