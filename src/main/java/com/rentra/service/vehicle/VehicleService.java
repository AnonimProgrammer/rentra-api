package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.*;

@Service
public interface VehicleService {
    List<VehicleSummary> search(VehicleSearchRequest request);

    VehicleDetails getDetails(UUID vehicleId);

    VehicleDetails create(UUID userId, CreateVehicleRequest request);

    ReservationResponse reserve(UUID userId, ReserveVehicleRequest request);

    RentResponse confirmReservation(UUID agencyUserId, UUID vehicleId, ConfirmReservationRequest request);

    VehicleSummary completeTechnicalCheck(UUID userId, UUID vehicleId);

    PageResponse<RentResponse> getRentHistory(UUID requesterUserId, UUID vehicleId, VehicleRentHistoryRequest request);
}
