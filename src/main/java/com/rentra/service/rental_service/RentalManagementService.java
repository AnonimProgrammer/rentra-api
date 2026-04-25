package com.rentra.service.rental_service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_service.RentalService;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.Vehicle;
import com.rentra.dto.rental_service.RentalServiceRequest;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_service.RentalServiceRepository;
import com.rentra.repository.user.UserRepository;

@Service
public class RentalManagementService {

    private final RentalServiceRepository rentalServiceRepository;
    private final UserRepository userRepository;

    public RentalManagementService(RentalServiceRepository rentalServiceRepository, UserRepository userRepository) {
        this.rentalServiceRepository = rentalServiceRepository;
        this.userRepository = userRepository;
    }

    public List<RentalService> findAll() {
        return rentalServiceRepository.findAll();
    }

    public RentalService findById(UUID id) {

        RentalService rentalService = rentalServiceRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalService not found for id: " + id));

        return rentalService;
    }

    public List<Vehicle> findVehiclesByRentalServiceId(UUID id) {

        RentalService rentalService = findById(id);
        return rentalService.getVehicles();
    }

    public RentalService create(RentalServiceRequest request) {

        RentalService rentalService = new RentalService();

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
