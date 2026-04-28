package com.rentra.service.rental_agency;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.rental_agency.RentalAgencyUserEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.dto.rental_agency.AgencyMembershipResponse;
import com.rentra.dto.rental_agency.UpdateAgencyMembership;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_agency.RentalAgencyUserRepository;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgencyMembershipService {
    private final RentalAgencyUserRepository rentalAgencyUserRepository;
    private final UserService userService;
    private final RentalAgencyService rentalAgencyService;
    private final AgencyAuthService agencyAuthService;

    @Transactional
    public AgencyMembershipResponse updateMembership(
            UUID agencyId, UUID confirmerUserId, UpdateAgencyMembership request) {
        UserEntity confirmer = userService.findOrThrow(confirmerUserId);
        RentalAgencyEntity agency = rentalAgencyService.findOrThrow(agencyId);

        agencyAuthService.verifyAuthorization(confirmer, agencyId, List.of(AgencyRole.MANAGER));

        if (request.userId().equals(agency.getOwnerUser().getId())) {
            throw new AuthorizationDeniedException("Agency owner membership can not be updated");
        }

        RentalAgencyUserEntity membership = rentalAgencyUserRepository
                .findMembership(request.userId(), agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

        if (request.status() != null) {
            membership.setStatus(request.status());
        }
        if (request.role() != null) {
            membership.setRole(request.role());
        }

        RentalAgencyUserEntity saved = rentalAgencyUserRepository.save(membership);
        return new AgencyMembershipResponse(
                saved.getUserId(), saved.getRentalAgencyId(), saved.getRole(), saved.getStatus());
    }
}
