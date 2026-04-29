package com.rentra.service.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.vehicle.*;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.vehicle.VehicleRepository;
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

    @Override
    @Transactional
    public VehicleDetails create(UUID userId, CreateVehicleRequest request) {
        UserEntity user = userService.findOrThrow(userId);
        RentalAgencyEntity rentalAgency = rentalAgencyService.findOrThrow(request.rentalAgencyId());

        agencyAuthService.verifyAuthority(user, rentalAgency.getId(), List.of(AgencyRole.MANAGER));

        VehicleEntity vehicleEntity = vehicleMapper.toEntity(request, rentalAgency);
        boolean hasRates = request.rates() != null && !request.rates().isEmpty();
        vehicleEntity.setStatus(hasRates ? VehicleStatus.AVAILABLE : VehicleStatus.INCOMPLETE);

        VehicleEntity savedVehicleEntity = vehicleRepository.save(vehicleEntity);
        return vehicleMapper.toDetails(savedVehicleEntity);
    }

    @Override
    @Transactional
    public VehicleDetails update(UUID userId, UUID vehicleId, UpdateVehicleRequest request) {
        UserEntity user = userService.findOrThrow(userId);
        VehicleEntity vehicle = findOrThrow(vehicleId);
        agencyAuthService.verifyAuthority(user, vehicle.getRentalAgency().getId(), List.of(AgencyRole.MANAGER));

        if (request.category() != null) vehicle.setCategory(request.category());
        if (request.brand() != null) vehicle.setBrand(request.brand());
        if (request.model() != null) vehicle.setModel(request.model());
        if (request.transmission() != null) vehicle.setTransmission(request.transmission());
        if (request.fuelType() != null) vehicle.setFuelType(request.fuelType());
        if (request.seatCount() != null) vehicle.setSeatCount(request.seatCount());

        if (request.rates() != null) {
            vehicle.getRates().clear();
            for (CreateVehicleRateRequest rateRequest : request.rates()) {
                VehicleRateEntity rate = vehicleMapper.toRateEntity(rateRequest);
                rate.setVehicle(vehicle);
                vehicle.getRates().add(rate);
            }

            if (vehicle.getRates().isEmpty() && vehicle.getStatus() == VehicleStatus.AVAILABLE) {
                vehicle.setStatus(VehicleStatus.INCOMPLETE);
            } else if (!vehicle.getRates().isEmpty() && vehicle.getStatus() == VehicleStatus.INCOMPLETE) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
            }
        }

        return vehicleMapper.toDetails(vehicleRepository.save(vehicle));
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
    public PageResponse<VehicleSummary> search(VehicleSearchRequest request) {
        return getVehiclesPage(request.agencyId(), request, VehicleStatus.AVAILABLE);
    }

    @Override
    public PageResponse<VehicleSummary> getAll(VehicleSearchRequest request) {
        return getVehiclesPage(null, request, request.status());
    }

    @Override
    public PageResponse<VehicleSummary> getByAgencyId(UUID userId, UUID agencyId, VehicleSearchRequest request) {
        UserEntity requester = userService.findOrThrow(userId);
        rentalAgencyService.findOrThrow(agencyId);

        agencyAuthService.verifyAuthority(requester, agencyId, List.of(AgencyRole.FRONT_AGENT, AgencyRole.MANAGER));
        return getVehiclesPage(agencyId, request, request.status());
    }

    @Override
    public VehicleDetails getDetails(UUID vehicleId) {
        return vehicleMapper.toDetails(findOrThrow(vehicleId));
    }

    private PageResponse<VehicleSummary> getVehiclesPage(
            UUID agencyId, VehicleSearchRequest request, VehicleStatus enforcedStatus) {
        int pageLimit = request.limit() != null ? Math.max(1, Math.min(request.limit(), DEFAULT_LIMIT)) : DEFAULT_LIMIT;
        List<VehicleEntity> vehicles = vehicleRepository.findVehicles(
                agencyId,
                toEnumName(request.category()),
                request.brand(),
                request.model(),
                toEnumName(request.transmission()),
                toEnumName(request.fuelType()),
                request.seatCount(),
                toEnumName(enforcedStatus),
                request.cursor(),
                pageLimit + 1);

        boolean hasNext = vehicles.size() > pageLimit;
        List<VehicleEntity> pageItems = hasNext ? vehicles.subList(0, pageLimit) : vehicles;
        String nextCursor = hasNext ? pageItems.getLast().getId().toString() : null;

        PaginationMeta pagination = new PaginationMeta(
                nextCursor,
                request.cursor() != null ? request.cursor().toString() : null,
                hasNext,
                request.cursor() != null,
                pageLimit);

        List<VehicleSummary> data =
                pageItems.stream().map(vehicleMapper::toSummary).toList();
        return new PageResponse<>(data, pagination);
    }

    private String toEnumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    @Override
    public VehicleEntity findOrThrow(UUID vehicleId) {
        return vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for id: " + vehicleId));
    }
}
