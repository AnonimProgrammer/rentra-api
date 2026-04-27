package com.rentra.service.vehicle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.user.UserEntity;
import com.rentra.dto.rent.RentResponse;
import com.rentra.mapper.RentMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.user.UserRepository;
import org.springframework.stereotype.Service;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.vehicle.*;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.rental_agency.RentalAgencyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final RentalAgencyService rentalAgencyService;
    private final VehicleMapper vehicleMapper;
    private final UserRepository userRepository;
    private final RentRepository rentRepository;
    private final RentMapper rentMapper;

    @Override
    @Transactional
    public VehicleDetails create(CreateVehicleRequest request) {
        RentalAgencyEntity rentalAgency = rentalAgencyService.findOrThrow(request.rentalAgencyId());

        VehicleEntity vehicleEntity = vehicleMapper.toEntity(request, rentalAgency);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return vehicleMapper.toDetails(savedVehicleEntity);
    }


    @Transactional
    public RentResponse confirmReservation(UUID vehicleId, UUID customerId) {
        UserEntity customer =
                userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        if (rentRepository.existsByCustomerIdAndStatus(customer.getId(), RentStatus.ACTIVE)) {
            throw new ConflictException("Rent is already active");
        }

        VehicleEntity vehicle = findOrThrow(vehicleId);
        if (vehicle.getStatus() == VehicleStatus.RESERVED) {
            throw new ConflictException("Vehicle is already reserved");
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

    @Transactional
    public ReservationResponse reserve(ReserveVehicleRequest request) {
        VehicleEntity vehicleEntity = findOrThrow(request.vehicleId());

        if (vehicleEntity.getStatus() != VehicleStatus.AVAILABLE) {
            throw new ConflictException("Vehicle is not available");
        }

        vehicleEntity.setStatus(VehicleStatus.PENDING);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return new ReservationResponse(savedVehicleEntity.getId(), savedVehicleEntity.getStatus());
    }

    @Override
    public List<VehicleSummary> search(VehicleSearchRequest request) {
        List<VehicleEntity> vehicleEntities = vehicleRepository.searchAvailableVehicles(
                request.category(),
                request.brand(),
                request.model(),
                request.transmission(),
                request.fuelType(),
                request.seatCount());
        return vehicleEntities.stream().map(vehicleMapper::toSummary).toList();
    }

    @Override
    public VehicleDetails getDetails(UUID vehicleId) {
        return vehicleMapper.toDetails(findOrThrow(vehicleId));
    }

    public VehicleEntity findOrThrow(UUID vehicleId) {
        return vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + vehicleId));
    }
}
