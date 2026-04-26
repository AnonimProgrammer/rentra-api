package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import com.rentra.domain.rental_service.RentalService;
import com.rentra.dto.vehicle.*;
import com.rentra.repository.rental_service.RentalServiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.rentra.domain.vehicle.Vehicle;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.vehicle.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    private final RentalServiceRepository rentalServiceRepository;
    public VehicleServiceImpl(VehicleRepository vehicleRepository, RentalServiceRepository rentalServiceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.rentalServiceRepository = rentalServiceRepository;
    }

    @Override
    public VehicleDetailsResponse createVehicle(CreateVehicleRequest request){
        RentalService rentalService = rentalServiceRepository.findById(request.rentalServiceId())
                .orElseThrow(() -> new RuntimeException("Rental Service Not Found"));

        Vehicle vehicle = VehicleMapper.toEntity(request,rentalService);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        boolean available = savedVehicle.getStatus() == VehicleStatus.AVAILABLE;
        return VehicleMapper.toDetailsResponse(savedVehicle, available);

    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available");
        }
        vehicle.setStatus(VehicleStatus.PENDING);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return new ReservationResponse(
                savedVehicle.getId(),
                savedVehicle.getStatus()
        );
    }
    @Override
    public List<VehicleSummaryResponse> searchVehicles(VehicleSearchRequest request) {
        List<Vehicle> vehicles = vehicleRepository.searchAvailableVehicles(
                request.category(),
                request.brand(),
                request.model(),
                request.transmission(),
                request.fuelType(),
                request.seatCount());
        return vehicles.stream().map(VehicleMapper::toSummaryResponse).toList();
    }

    @Override
    public VehicleDetailsResponse getVehicleDetails(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + vehicleId));
        boolean available = vehicle.getStatus() == VehicleStatus.AVAILABLE;
        return VehicleMapper.toDetailsResponse(vehicle, available);
    }
}
