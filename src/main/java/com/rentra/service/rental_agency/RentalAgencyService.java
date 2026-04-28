package com.rentra.service.rental_agency;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.rental_agency.RentalAgencyStatus;
import com.rentra.domain.rental_agency.RentalAgencyUserEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.rental_agency.CreateRentalAgencyRequest;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentalAgencyMapper;
import com.rentra.repository.rental_agency.RentalAgencyRepository;
import com.rentra.repository.rental_agency.RentalAgencyUserRepository;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalAgencyService {
    private final RentalAgencyRepository rentalAgencyRepository;
    private final UserService userService;
    private final RentalAgencyMapper rentalAgencyMapper;
    private final RentalAgencyUserRepository rentalAgencyUserRepository;

    @Transactional
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
        createOwnerMembership(savedAgency.getId(), ownerId);

        return rentalAgencyMapper.toResponse(savedAgency);
    }

    public RentalAgencyEntity findOrThrow(UUID id) {
        return rentalAgencyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental agency not found for id: " + id));
    }

    private void createOwnerMembership(UUID agencyId, UUID ownerId) {
        RentalAgencyUserEntity ownerMembership = new RentalAgencyUserEntity();
        ownerMembership.setRentalAgencyId(agencyId);
        ownerMembership.setUserId(ownerId);
        ownerMembership.setRole(AgencyRole.MANAGER);
        ownerMembership.setStatus(UserStatus.ACTIVE);
        rentalAgencyUserRepository.save(ownerMembership);
    }
}
