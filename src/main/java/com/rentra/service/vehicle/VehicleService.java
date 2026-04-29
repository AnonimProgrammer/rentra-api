package com.rentra.service.vehicle;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.vehicle.*;

@Service
public interface VehicleService {
    PageResponse<VehicleSummary> search(VehicleSearchRequest request);

    PageResponse<VehicleSummary> getAll(VehicleSearchRequest request);

    PageResponse<VehicleSummary> getByAgencyId(UUID userId, UUID agencyId, VehicleSearchRequest request);

    VehicleDetails getDetails(UUID vehicleId);

    VehicleDetails create(UUID userId, CreateVehicleRequest request);

    VehicleDetails update(UUID userId, UUID vehicleId, UpdateVehicleRequest request);

    VehicleSummary completeTechnicalCheck(UUID userId, UUID vehicleId);

    VehicleEntity findOrThrow(UUID vehicleId);
}
