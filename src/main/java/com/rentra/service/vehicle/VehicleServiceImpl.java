package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
    public VehicleDetails create(CreateVehicleRequest request) {
        RentalAgencyEntity rentalAgency = rentalAgencyService.findOrThrow(request.rentalAgencyId());

        VehicleEntity vehicleEntity = vehicleMapper.toEntity(request, rentalAgency);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return vehicleMapper.toDetails(savedVehicleEntity);
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
