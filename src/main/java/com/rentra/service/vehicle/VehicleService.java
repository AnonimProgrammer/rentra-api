package com.rentra.service.vehicle;

import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;
import com.rentra.mapper.VehicleMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface VehicleService {


    List<VehicleSummaryResponse> searchVehicles(VehicleSearchRequest request);

    VehicleDetailsResponse getVehicleDetails(UUID vehicleId);
}
