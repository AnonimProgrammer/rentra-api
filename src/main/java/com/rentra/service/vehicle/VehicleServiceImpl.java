package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_service.RentalServiceEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.vehicle.*;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.rental_service.RentalServiceRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    private final RentalServiceRepository rentalServiceRepository;

    @Override
    public VehicleDetailsResponse createVehicle(CreateVehicleRequest request) {
        RentalServiceEntity rentalService = rentalServiceRepository
                .findById(request.rentalServiceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rental Service not found for id: " + request.rentalServiceId()));

        VehicleEntity vehicleEntity = VehicleMapper.toEntity(request, rentalService);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        boolean available = savedVehicleEntity.getStatus() == VehicleStatus.AVAILABLE;
        return VehicleMapper.toDetailsResponse(savedVehicleEntity, available);
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        VehicleEntity vehicleEntity = vehicleRepository
                .findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + request.vehicleId()));
        if (vehicleEntity.getStatus() != VehicleStatus.AVAILABLE) {
            throw new ConflictException("Vehicle is not available");
        }
        vehicleEntity.setStatus(VehicleStatus.PENDING);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return new ReservationResponse(savedVehicleEntity.getId(), savedVehicleEntity.getStatus());
    }

    @Override
    public List<VehicleSummaryResponse> searchVehicles(VehicleSearchRequest request) {
        List<VehicleEntity> vehicleEntities = vehicleRepository.searchAvailableVehicles(
                request.category(),
                request.brand(),
                request.model(),
                request.transmission(),
                request.fuelType(),
                request.seatCount());
        return vehicleEntities.stream().map(VehicleMapper::toSummaryResponse).toList();
    }

    @Override
    public VehicleDetailsResponse getVehicleDetails(UUID vehicleId) {
        VehicleEntity vehicleEntity = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + vehicleId));
        boolean available = vehicleEntity.getStatus() == VehicleStatus.AVAILABLE;
        return VehicleMapper.toDetailsResponse(vehicleEntity, available);
    }
}
