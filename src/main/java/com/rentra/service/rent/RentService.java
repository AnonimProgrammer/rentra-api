package com.rentra.service.rent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.VehicleRentHistoryRequest;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.price.PriceEngine;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import com.rentra.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentService {
    private static final Integer DEFAULT_LIMIT = 20;
    private final RentRepository rentRepository;
    private final VehicleRepository vehicleRepository;
    private final PriceEngine priceEngine;
    private final RentMapper rentMapper;
    private final UserService userService;
    private final AgencyAuthService agencyAuthService;
    private final VehicleService vehicleService;

    @Transactional
    public RentResponse complete(UUID rentId, UUID customerId) {
        RentEntity rent = findOrThrow(rentId);
        verifyAuthority(rent, customerId);

        if (rent.getStatus() != RentStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE rent can be completed");
        }

        rent.setCompletedAt(OffsetDateTime.now());
        rent.setStatus(RentStatus.COMPLETED);
        rent.setTotalAmount(priceEngine.calculateFinalAmount(rent));

        VehicleEntity vehicle = rent.getVehicle();
        vehicle.setStatus(VehicleStatus.TECHNICAL_CHECK);
        vehicleRepository.save(vehicle);

        return rentMapper.toResponse(rentRepository.save(rent));
    }

    @Transactional
    public RentResponse rate(UUID rentId, UUID customerId, Integer rating) {
        RentEntity rent = findOrThrow(rentId);
        verifyAuthority(rent, customerId);

        if (rent.getStatus() != RentStatus.COMPLETED) {
            throw new IllegalArgumentException("Rating is only allowed for COMPLETED rent");
        }

        rent.setRating(rating);
        return rentMapper.toResponse(rentRepository.save(rent));
    }

    private void verifyAuthority(RentEntity rent, UUID customerId) {
        if (!rent.getCustomer().getId().equals(customerId)) {
            throw new AuthorizationDeniedException("Access denied for rent operation");
        }
    }

    @Transactional
    public RentEntity create(UserEntity customer, VehicleEntity vehicle) {
        RentEntity rent = new RentEntity();
        rent.setCustomer(customer);
        rent.setVehicle(vehicle);
        rent.setRentalAgency(vehicle.getRentalAgency());
        rent.setStatus(RentStatus.ACTIVE);
        rent.setTotalAmount(BigDecimal.ZERO);
        rent.setStartsAt(OffsetDateTime.now());

        return rentRepository.save(rent);
    }

    public RentResponse getMyActive(UUID userId) {
        RentEntity rent = rentRepository
                .findFirstByCustomerIdAndStatusOrderByIdDesc(userId, RentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have an active rent"));
        return rentMapper.toResponse(rent);
    }

    public RentEntity findOrThrow(UUID rentId) {
        return rentRepository
                .findById(rentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent not found for id: " + rentId));
    }

    public PageResponse<RentResponse> getMyRents(UUID userId, UUID cursor, Integer limit, RentStatus status) {
        return getRentsPage(userId, null, cursor, limit, status, null, null, null, null, null, null, null, null);
    }

    public PageResponse<RentResponse> getRentHistoryByVehicleId(
            UUID userId, UUID vehicleId, VehicleRentHistoryRequest request) {
        UserEntity requester = userService.findOrThrow(userId);
        VehicleEntity vehicle = vehicleService.findOrThrow(vehicleId);

        agencyAuthService.verifyAuthority(
                requester, vehicle.getRentalAgency().getId(), List.of(AgencyRole.MANAGER, AgencyRole.FRONT_AGENT));
        validateRentHistoryFilters(request);

        return getRentsPage(
                null,
                vehicleId,
                request.cursor(),
                request.limit(),
                request.status(),
                request.startedFrom(),
                request.startedTo(),
                request.completedFrom(),
                request.completedTo(),
                request.minTotalAmount(),
                request.maxTotalAmount(),
                request.minRating(),
                request.maxRating());
    }

    private PageResponse<RentResponse> getRentsPage(
            UUID customerId,
            UUID vehicleId,
            UUID cursor,
            Integer limit,
            RentStatus status,
            OffsetDateTime startedFrom,
            OffsetDateTime startedTo,
            OffsetDateTime completedFrom,
            OffsetDateTime completedTo,
            BigDecimal minTotalAmount,
            BigDecimal maxTotalAmount,
            Integer minRating,
            Integer maxRating) {
        int pageLimit = limit != null ? Math.max(1, Math.min(limit, DEFAULT_LIMIT)) : DEFAULT_LIMIT;

        List<RentEntity> rents = rentRepository.findRents(
                customerId,
                vehicleId,
                status != null ? status.name() : null,
                startedFrom,
                startedTo,
                completedFrom,
                completedTo,
                minTotalAmount,
                maxTotalAmount,
                minRating,
                maxRating,
                cursor,
                pageLimit + 1);

        boolean hasNext = rents.size() > pageLimit;
        List<RentEntity> pageItems = hasNext ? rents.subList(0, pageLimit) : rents;
        String nextCursor = hasNext ? pageItems.getLast().getId().toString() : null;

        PaginationMeta meta = new PaginationMeta(
                nextCursor, (cursor != null) ? cursor.toString() : null, hasNext, cursor != null, pageLimit);

        List<RentResponse> responses =
                pageItems.stream().map(rentMapper::toResponse).toList();
        return new PageResponse<>(responses, meta);
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
}
