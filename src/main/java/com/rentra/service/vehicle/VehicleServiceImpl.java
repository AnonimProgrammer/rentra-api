package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.*;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.rent.RentService;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private static final Integer DEFAULT_LIMIT = 20;
    private final VehicleRepository vehicleRepository;
    private final RentalAgencyService rentalAgencyService;
    private final VehicleMapper vehicleMapper;
    private final UserService userService;
    private final AgencyAuthService agencyAuthService;
    private final RentRepository rentRepository;
    private final RentMapper rentMapper;
    private final RentService rentService;

    @Override
    @Transactional
    public VehicleDetails create(UUID userId, CreateVehicleRequest request) {
        UserEntity user = userService.findOrThrow(userId);
        RentalAgencyEntity rentalAgency = rentalAgencyService.findOrThrow(request.rentalAgencyId());

        agencyAuthService.verifyAuthority(user, rentalAgency.getId(), List.of(AgencyRole.MANAGER));

        VehicleEntity vehicleEntity = vehicleMapper.toEntity(request, rentalAgency);
        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return vehicleMapper.toDetails(savedVehicleEntity);
    }

    @Override
    @Transactional
    public RentResponse confirmReservation(UUID agencyUserId, UUID vehicleId, ConfirmReservationRequest request) {
        UserEntity agencyUser = userService.findOrThrow(agencyUserId);
        UserEntity customer = userService.findOrThrow(request.customerId());
        VehicleEntity vehicle = findOrThrow(vehicleId);

        agencyAuthService.verifyAuthority(
                agencyUser, vehicle.getRentalAgency().getId(), List.of(AgencyRole.MANAGER, AgencyRole.FRONT_AGENT));

        if (rentRepository.existsByCustomerIdAndStatus(customer.getId(), RentStatus.ACTIVE)) {
            throw new IllegalArgumentException("Customer already has an active rent");
        }
        if (vehicle.getStatus() != VehicleStatus.PENDING) {
            throw new ConflictException("Invalid vehicle status for this operation");
        }

        RentEntity savedRent = rentService.create(customer, vehicle);
        vehicle.setStatus(VehicleStatus.RENTED);

        return rentMapper.toResponse(savedRent);
    }

    @Override
    @Transactional
    public VehicleSummary completeTechnicalCheck(UUID userId, UUID vehicleId) {
        UserEntity user = userService.findOrThrow(userId);
        VehicleEntity vehicle = findOrThrow(vehicleId);

        agencyAuthService.verifyAuthority(
                user, vehicle.getRentalAgency().getId(), List.of(AgencyRole.MANAGER, AgencyRole.FRONT_AGENT));

        if (vehicle.getStatus() != VehicleStatus.TECHNICAL_CHECK) {
            throw new ConflictException("Vehicle must be in TECHNICAL_CHECK state");
        }
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        return vehicleMapper.toSummary(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public ReservationResponse reserve(UUID userId, ReserveVehicleRequest request) {
        VehicleEntity vehicleEntity = findOrThrow(request.vehicleId());

        boolean hasActiveRent = rentRepository.existsByCustomerIdAndStatus(userId, RentStatus.ACTIVE);
        if (hasActiveRent) {
            throw new ConflictException("Customer already has an active rent. Cannot create a new reservation.");
        }

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

    @Override
    public PageResponse<RentResponse> getRentHistory(
            UUID requesterUserId, UUID vehicleId, VehicleRentHistoryRequest request) {
        UserEntity requester = userService.findOrThrow(requesterUserId);
        VehicleEntity vehicle = findOrThrow(vehicleId);

        agencyAuthService.verifyAuthority(
                requester, vehicle.getRentalAgency().getId(), List.of(AgencyRole.MANAGER, AgencyRole.FRONT_AGENT));

        validateRentHistoryFilters(request);

        int pageLimit = request.limit() != null ? Math.max(1, Math.min(request.limit(), DEFAULT_LIMIT)) : DEFAULT_LIMIT;
        List<RentEntity> rents = rentRepository.findVehicleRentHistory(
                vehicleId,
                request.status() != null ? request.status().name() : null,
                request.startedFrom(),
                request.startedTo(),
                request.completedFrom(),
                request.completedTo(),
                request.minTotalAmount(),
                request.maxTotalAmount(),
                request.minRating(),
                request.maxRating(),
                request.cursor(),
                pageLimit + 1);

        boolean hasNext = rents.size() > pageLimit;
        List<RentEntity> pageItems = hasNext ? rents.subList(0, pageLimit) : rents;
        String nextCursor = hasNext ? pageItems.getLast().getId().toString() : null;

        PaginationMeta pagination = new PaginationMeta(
                nextCursor,
                request.cursor() != null ? request.cursor().toString() : null,
                hasNext,
                request.cursor() != null,
                pageLimit);

        List<RentResponse> data = pageItems.stream().map(rentMapper::toResponse).toList();
        return new PageResponse<>(data, pagination);
    }

    private void validateRentHistoryFilters(VehicleRentHistoryRequest request) {
        if (request.startedFrom() != null
                && request.startedTo() != null
                && request.startedFrom().isAfter(request.startedTo())) {
            throw new IllegalArgumentException("startedFrom must be earlier than or equal to startedTo");
        }

        if (request.completedFrom() != null
                && request.completedTo() != null
                && request.completedFrom().isAfter(request.completedTo())) {
            throw new IllegalArgumentException("completedFrom must be earlier than or equal to completedTo");
        }

        if (request.minTotalAmount() != null
                && request.maxTotalAmount() != null
                && request.minTotalAmount().compareTo(request.maxTotalAmount()) > 0) {
            throw new IllegalArgumentException("minTotalAmount must be less than or equal to maxTotalAmount");
        }

        if (request.minRating() != null && request.maxRating() != null && request.minRating() > request.maxRating()) {
            throw new IllegalArgumentException("minRating must be less than or equal to maxRating");
        }
    }

    public VehicleEntity findOrThrow(UUID vehicleId) {
        return vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + vehicleId));
    }
}
