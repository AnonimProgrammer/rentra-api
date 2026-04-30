package com.rentra.service.rental_agency;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.rental_agency.MyAgencyMembershipProjection;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.rental_agency.RentalAgencyUserEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rental_agency.AgencyMembershipResponse;
import com.rentra.dto.rental_agency.MyAgencyMembershipResponse;
import com.rentra.dto.rental_agency.UpdateAgencyMembership;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.rental_agency.RentalAgencyUserRepository;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyMembershipServiceTest {
    @Mock
    private RentalAgencyUserRepository rentalAgencyUserRepository;

    @Mock
    private UserService userService;

    @Mock
    private RentalAgencyService rentalAgencyService;

    @Mock
    private AgencyAuthService agencyAuthService;

    @InjectMocks
    private AgencyMembershipService agencyMembershipService;

    // =========================== getMyMemberships ===========================

    @Test
    void getMyMemberships_shouldReturnMappedMemberships() {
        UUID userId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        OffsetDateTime joinedAt = OffsetDateTime.parse("2026-03-01T10:00:00Z");
        MyAgencyMembershipProjection projection = createProjection(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Rentra Baku",
                AgencyRole.MANAGER,
                UserStatus.ACTIVE,
                joinedAt);

        when(userService.findOrThrow(userId)).thenReturn(user);
        when(rentalAgencyUserRepository.findMyMemberships(userId)).thenReturn(List.of(projection));

        List<MyAgencyMembershipResponse> result = agencyMembershipService.getMyMemberships(userId);

        assertEquals(1, result.size());
        MyAgencyMembershipResponse item = result.getFirst();
        assertEquals(projection.getAgencyId(), item.agencyId());
        assertEquals(projection.getAgencyName(), item.agencyName());
        assertEquals(projection.getRole(), item.role());
        assertEquals(projection.getStatus(), item.status());
        assertEquals(projection.getJoinedAt(), item.joinedAt());
        verify(userService).findOrThrow(userId);
        verify(rentalAgencyUserRepository).findMyMemberships(userId);
    }

    // =========================== getMemberships ===========================

    @Test
    void getMemberships_shouldReturnPaginatedData_whenLimitIsNull() {
        UUID agencyId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UserEntity requester = createUser(requesterId);

        RentalAgencyUserEntity membership = new RentalAgencyUserEntity();
        membership.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        membership.setUserId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        membership.setRentalAgencyId(agencyId);
        membership.setRole(AgencyRole.FRONT_AGENT);
        membership.setStatus(UserStatus.ACTIVE);

        when(userService.findOrThrow(requesterId)).thenReturn(requester);
        when(rentalAgencyUserRepository.findAgencyMemberships(agencyId, null, null, null, 21))
                .thenReturn(List.of(membership));

        PageResponse<AgencyMembershipResponse> result =
                agencyMembershipService.getMemberships(agencyId, requesterId, null, null, null, null);

        assertEquals(1, result.data().size());
        assertEquals(20, result.pagination().limit());
        assertFalse(result.pagination().hasNext());
        assertFalse(result.pagination().hasPrevious());
        assertNull(result.pagination().nextCursor());
        AgencyMembershipResponse first = result.data().getFirst();
        assertEquals(membership.getUserId(), first.userId());
        assertEquals(membership.getRentalAgencyId(), first.agencyId());
        assertEquals(membership.getRole(), first.role());
        assertEquals(membership.getStatus(), first.status());

        verify(userService).findOrThrow(requesterId);
        verify(rentalAgencyService).findOrThrow(agencyId);
        verify(agencyAuthService)
                .verifyAuthority(requester, agencyId, List.of(AgencyRole.FRONT_AGENT, AgencyRole.MANAGER));
    }

    @Test
    void getMemberships_shouldClampLimitAndSetNextCursor_whenHasMoreItemsThanPageLimit() {
        UUID agencyId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        UserEntity requester = createUser(requesterId);

        RentalAgencyUserEntity first = new RentalAgencyUserEntity();
        first.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        first.setUserId(UUID.randomUUID());
        first.setRentalAgencyId(agencyId);
        first.setRole(AgencyRole.MANAGER);
        first.setStatus(UserStatus.ACTIVE);

        List<RentalAgencyUserEntity> memberships = new ArrayList<>();
        memberships.add(first);
        for (int i = 0; i < 20; i++) {
            RentalAgencyUserEntity extra = new RentalAgencyUserEntity();
            extra.setId(UUID.randomUUID());
            extra.setUserId(UUID.randomUUID());
            extra.setRentalAgencyId(agencyId);
            extra.setRole(AgencyRole.FRONT_AGENT);
            extra.setStatus(UserStatus.ACTIVE);
            memberships.add(extra);
        }

        when(userService.findOrThrow(requesterId)).thenReturn(requester);
        when(rentalAgencyUserRepository.findAgencyMemberships(agencyId, "MANAGER", "ACTIVE", cursor, 21))
                .thenReturn(memberships);

        PageResponse<AgencyMembershipResponse> result = agencyMembershipService.getMemberships(
                agencyId, requesterId, cursor, 50, AgencyRole.MANAGER, UserStatus.ACTIVE);

        assertEquals(20, result.data().size());
        assertEquals(20, result.pagination().limit());
        assertTrue(result.pagination().hasNext());
        assertTrue(result.pagination().hasPrevious());
        assertEquals(memberships.get(19).getId().toString(), result.pagination().nextCursor());
        assertEquals(cursor.toString(), result.pagination().previousCursor());
    }

    // =========================== updateMembership ===========================

    @Test
    void updateMembership_shouldThrowAuthorizationDeniedException_whenTargetUserIsAgencyOwner() {
        UUID agencyId = UUID.randomUUID();
        UUID confirmerId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UserEntity confirmer = createUser(confirmerId);

        UserEntity owner = createUser(ownerId);
        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setOwnerUser(owner);

        UpdateAgencyMembership request =
                new UpdateAgencyMembership(ownerId, UserStatus.BLOCKED, AgencyRole.FRONT_AGENT);

        when(userService.findOrThrow(confirmerId)).thenReturn(confirmer);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(agency);

        AuthorizationDeniedException exception = assertThrows(
                AuthorizationDeniedException.class,
                () -> agencyMembershipService.updateMembership(agencyId, confirmerId, request));

        assertEquals("Agency owner membership can not be updated", exception.getMessage());
    }

    @Test
    void updateMembership_shouldThrowResourceNotFoundException_whenMembershipDoesNotExist() {
        UUID agencyId = UUID.randomUUID();
        UUID confirmerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UserEntity confirmer = createUser(confirmerId);

        UserEntity owner = createUser(UUID.randomUUID());
        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setOwnerUser(owner);
        UpdateAgencyMembership request =
                new UpdateAgencyMembership(targetUserId, UserStatus.ACTIVE, AgencyRole.MANAGER);

        when(userService.findOrThrow(confirmerId)).thenReturn(confirmer);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(agency);
        when(rentalAgencyUserRepository.findMembership(targetUserId, agencyId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> agencyMembershipService.updateMembership(agencyId, confirmerId, request));

        assertEquals("Membership not found", exception.getMessage());
    }

    @Test
    void updateMembership_shouldUpdateRoleAndStatusAndReturnResponse() {
        UUID agencyId = UUID.randomUUID();
        UUID confirmerId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UserEntity confirmer = createUser(confirmerId);

        UserEntity owner = createUser(UUID.randomUUID());
        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setOwnerUser(owner);

        RentalAgencyUserEntity membership = new RentalAgencyUserEntity();
        membership.setUserId(targetUserId);
        membership.setRentalAgencyId(agencyId);
        membership.setRole(AgencyRole.FRONT_AGENT);
        membership.setStatus(UserStatus.ACTIVE);

        UpdateAgencyMembership request =
                new UpdateAgencyMembership(targetUserId, UserStatus.BLOCKED, AgencyRole.MANAGER);

        when(userService.findOrThrow(confirmerId)).thenReturn(confirmer);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(agency);
        when(rentalAgencyUserRepository.findMembership(targetUserId, agencyId)).thenReturn(Optional.of(membership));
        when(rentalAgencyUserRepository.save(membership)).thenReturn(membership);

        AgencyMembershipResponse result = agencyMembershipService.updateMembership(agencyId, confirmerId, request);

        assertEquals(targetUserId, result.userId());
        assertEquals(agencyId, result.agencyId());
        assertEquals(AgencyRole.MANAGER, result.role());
        assertEquals(UserStatus.BLOCKED, result.status());
    }

    private UserEntity createUser(UUID userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setFirstName("Omar");
        user.setLastName("Ismayilov");
        user.setEmail("omar.ismayilov@example.com");
        user.setPhoneNumber("+994 99 999 99 99");
        user.setBirthDate(LocalDate.of(1995, 5, 15));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        user.setUpdatedAt(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        return user;
    }

    private MyAgencyMembershipProjection createProjection(
            UUID agencyId, String agencyName, AgencyRole role, UserStatus status, OffsetDateTime joinedAt) {
        return new MyAgencyMembershipProjection() {
            @Override
            public UUID getAgencyId() {
                return agencyId;
            }

            @Override
            public String getAgencyName() {
                return agencyName;
            }

            @Override
            public AgencyRole getRole() {
                return role;
            }

            @Override
            public UserStatus getStatus() {
                return status;
            }

            @Override
            public OffsetDateTime getJoinedAt() {
                return joinedAt;
            }
        };
    }
}
