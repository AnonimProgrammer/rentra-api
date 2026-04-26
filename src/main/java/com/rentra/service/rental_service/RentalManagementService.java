package com.rentra.service.rental_service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_service.RentalServiceEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.dto.rental_service.RentalServiceRequest;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_service.RentalServiceRepository;
import com.rentra.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalManagementService {

    private final RentalServiceRepository rentalServiceRepository;
    private final UserRepository userRepository;

    public List<RentalServiceEntity> findAll() {
        return rentalServiceRepository.findAll();
    }

    public RentalServiceEntity findById(UUID id) {

        RentalServiceEntity rentalService = rentalServiceRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalService not found for id: " + id));

        return rentalService;
    }

    public List<VehicleEntity> findVehiclesByRentalServiceId(UUID id) {

        RentalServiceEntity rentalService = findById(id);
        return rentalService.getVehicles();
    }

    public RentalServiceEntity create(RentalServiceRequest request) {

        RentalServiceEntity rentalService = new RentalServiceEntity();

        UserEntity owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + request.getOwnerId()));

        rentalService.setOwnerUser(owner);

        rentalService.setName(request.getName());

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            rentalService.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            rentalService.setStatus(request.getStatus());
        }

        rentalService.setLocationLat(request.getLocationLat());
        rentalService.setLocationLng(request.getLocationLng());

        rentalServiceRepository.save(rentalService);

        return rentalService;
    }
}
