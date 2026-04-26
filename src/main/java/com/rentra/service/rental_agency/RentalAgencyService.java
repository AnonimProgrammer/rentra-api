package com.rentra.service.rental_agency;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.rental_agency.RentalAgencyStatus;
import com.rentra.domain.user.UserEntity;
import com.rentra.dto.rental_agency.CreateRentalAgencyRequest;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentalAgencyMapper;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.rental_agency.RentalAgencyRepository;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalAgencyService {
    private final RentalAgencyRepository rentalAgencyRepository;
    private final UserService userService;
    private final RentalAgencyMapper rentalAgencyMapper;
    private final VehicleMapper vehicleMapper;

    public RentalAgencyResponse create(CreateRentalAgencyRequest request, UUID ownerId) {
        UserEntity owner = userService.findOrThrow(ownerId);

        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setOwnerUser(owner);
        agency.setName(request.name());
        agency.setStatus(RentalAgencyStatus.ACTIVE);
        agency.setDescription(request.description());
        agency.setLocationLat(request.locationLat());
        agency.setLocationLng(request.locationLng());

        RentalAgencyEntity savedAgency = rentalAgencyRepository.save(agency);
        return rentalAgencyMapper.toResponse(savedAgency);
    }

    public RentalAgencyResponse getById(UUID id) {
        return rentalAgencyMapper.toResponse(findOrThrow(id));
    }

    public List<VehicleSummary> findVehiclesByAgency(UUID id) {
        RentalAgencyEntity agency = findOrThrow(id);
        return agency.getVehicles().stream().map(vehicleMapper::toSummary).toList();
    }

    public RentalAgencyEntity findOrThrow(UUID id) {
        return rentalAgencyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental agency not found for id: " + id));
    }
}
