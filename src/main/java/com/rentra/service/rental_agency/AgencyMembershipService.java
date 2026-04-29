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
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.rental_agency.AgencyMembershipResponse;
import com.rentra.dto.rental_agency.MyAgencyMembershipResponse;
import com.rentra.dto.rental_agency.UpdateAgencyMembership;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_agency.RentalAgencyUserRepository;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgencyMembershipService {
    private static final Integer DEFAULT_LIMIT = 20;
    private final RentalAgencyUserRepository rentalAgencyUserRepository;
    private final UserService userService;
    private final RentalAgencyService rentalAgencyService;
    private final AgencyAuthService agencyAuthService;

    public List<MyAgencyMembershipResponse> getMyMemberships(UUID userId) {
        UserEntity user = userService.findOrThrow(userId);
        return rentalAgencyUserRepository.findMyMemberships(user.getId()).stream()
                .map(item -> new MyAgencyMembershipResponse(
                        item.getAgencyId(), item.getAgencyName(), item.getRole(), item.getStatus(), item.getJoinedAt()))
                .toList();
    }

    public PageResponse<AgencyMembershipResponse> getMemberships(
            UUID agencyId, UUID requesterUserId, UUID cursor, Integer limit, AgencyRole role, UserStatus status) {
        UserEntity requester = userService.findOrThrow(requesterUserId);
        rentalAgencyService.findOrThrow(agencyId);

        agencyAuthService.verifyAuthority(requester, agencyId, List.of(AgencyRole.FRONT_AGENT, AgencyRole.MANAGER));

        int pageLimit = limit != null ? Math.max(1, Math.min(limit, DEFAULT_LIMIT)) : DEFAULT_LIMIT;

        List<RentalAgencyUserEntity> memberships = rentalAgencyUserRepository.findAgencyMemberships(
                agencyId,
                role != null ? role.name() : null,
                status != null ? status.name() : null,
                cursor,
                pageLimit + 1);

        boolean hasNext = memberships.size() > pageLimit;
        List<RentalAgencyUserEntity> pageItems = hasNext ? memberships.subList(0, pageLimit) : memberships;
        String nextCursor = hasNext ? pageItems.getLast().getId().toString() : null;

        PaginationMeta pagination = new PaginationMeta(
                nextCursor, cursor != null ? cursor.toString() : null, hasNext, cursor != null, pageLimit);

        List<AgencyMembershipResponse> data = pageItems.stream()
                .map(item -> new AgencyMembershipResponse(
                        item.getUserId(), item.getRentalAgencyId(), item.getRole(), item.getStatus()))
                .toList();

        return new PageResponse<>(data, pagination);
    }

    @Transactional
    public AgencyMembershipResponse updateMembership(
            UUID agencyId, UUID confirmerUserId, UpdateAgencyMembership request) {
        UserEntity confirmer = userService.findOrThrow(confirmerUserId);
        RentalAgencyEntity agency = rentalAgencyService.findOrThrow(agencyId);

        agencyAuthService.verifyAuthority(confirmer, agencyId, List.of(AgencyRole.MANAGER));

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
