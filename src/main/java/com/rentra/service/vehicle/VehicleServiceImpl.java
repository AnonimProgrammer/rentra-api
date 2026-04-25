package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.vehicle.Vehicle;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.vehicle.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
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
