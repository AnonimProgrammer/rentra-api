package com.rentra.service.rental_agency;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.dto.rental_agency.RentalAgencyRequest;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_agency.RentalAgencyRepository;
import com.rentra.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalAgencyService {

    private final RentalAgencyRepository rentalAgencyRepository;
    private final UserRepository userRepository;

    public List<RentalAgencyEntity> findAll() {
        return rentalAgencyRepository.findAll();
    }

    public RentalAgencyEntity findById(UUID id) {
        return rentalAgencyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalAgency not found for id: " + id));
    }

    public List<VehicleEntity> findVehiclesByRentalAgencyId(UUID id) {
        RentalAgencyEntity rentalAgency = findById(id);
        return rentalAgency.getVehicles();
    }

    public RentalAgencyEntity create(RentalAgencyRequest request) {
        RentalAgencyEntity rentalAgency = new RentalAgencyEntity();

        UserEntity owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + request.getOwnerId()));

        rentalAgency.setOwnerUser(owner);
        rentalAgency.setName(request.getName());

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            rentalAgency.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            rentalAgency.setStatus(request.getStatus());
        }

        rentalAgency.setLocationLat(request.getLocationLat());
        rentalAgency.setLocationLng(request.getLocationLng());

        rentalAgencyRepository.save(rentalAgency);

        return rentalAgency;
    }
}
