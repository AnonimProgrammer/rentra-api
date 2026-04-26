package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;

@Service
public interface VehicleService {
    List<VehicleSummaryResponse> searchVehicles(VehicleSearchRequest request);

    VehicleDetailsResponse getVehicleDetails(UUID vehicleId);

    VehicleDetailsResponse createVehicle(CreateVehicleRequest request);
}
