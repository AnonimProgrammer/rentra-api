package com.rentra.service.security.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.rental_agency.RentalAgencyUserEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.rental_agency.ConfirmJoinRequest;
import com.rentra.dto.rental_agency.ConfirmJoinResponse;
import com.rentra.dto.rental_agency.RequestJoinResponse;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_agency.RentalAgencyUserRepository;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgencyAuthService {
    private final RentalAgencyUserRepository rentalAgencyUserRepository;
    private final UserService userService;
    private final RentalAgencyService rentalAgencyService;

    @Transactional
    public RequestJoinResponse requestAuthorization(UUID agencyId, UUID userId) {
        UserEntity user = userService.findOrThrow(userId);
        RentalAgencyEntity agency = rentalAgencyService.findOrThrow(agencyId);

        RentalAgencyUserEntity membership = rentalAgencyUserRepository
                .findMembership(user.getId(), agency.getId())
                .map(existing -> {
                    if (existing.getStatus() == UserStatus.ACTIVE) {
                        throw new ConflictException("User is already authorized for this agency");
                    }
                    if (existing.getStatus() == UserStatus.BLOCKED) {
                        throw new ConflictException("User is blocked for this agency");
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    RentalAgencyUserEntity newMembership = new RentalAgencyUserEntity();
                    newMembership.setRentalAgencyId(agency.getId());
                    newMembership.setUserId(user.getId());
                    newMembership.setRole(AgencyRole.FRONT_AGENT);
                    newMembership.setStatus(UserStatus.PENDING);
                    return rentalAgencyUserRepository.save(newMembership);
                });

        return new RequestJoinResponse(membership.getUserId(), membership.getRentalAgencyId(), membership.getStatus());
    }

    @Transactional
    public ConfirmJoinResponse confirmAuthorization(UUID agencyId, UUID userId, ConfirmJoinRequest request) {
        UserEntity confirmer = userService.findOrThrow(userId);
        rentalAgencyService.findOrThrow(agencyId);

        verifyAuthorization(confirmer, agencyId, List.of(AgencyRole.MANAGER));

        RentalAgencyUserEntity membership = rentalAgencyUserRepository
                .findMembership(request.requestedUserId(), agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership request not found"));

        if (membership.getStatus() != UserStatus.PENDING) {
            throw new ConflictException("Only PENDING request can be confirmed");
        }

        membership.setRole(request.roleToAssign());
        membership.setStatus(UserStatus.ACTIVE);
        RentalAgencyUserEntity savedMembership = rentalAgencyUserRepository.save(membership);
        return new ConfirmJoinResponse(
                savedMembership.getUserId(),
                savedMembership.getRentalAgencyId(),
                savedMembership.getRole(),
                savedMembership.getStatus());
    }

    public void verifyAuthorization(UserEntity user, UUID agencyId, List<AgencyRole> roles) {
        boolean authorized =
                rentalAgencyUserRepository.hasAuthorization(user.getId(), agencyId, UserStatus.ACTIVE, roles);
        if (!authorized) {
            throw new AuthorizationDeniedException("Access denied for agency operation");
        }
    }
}
