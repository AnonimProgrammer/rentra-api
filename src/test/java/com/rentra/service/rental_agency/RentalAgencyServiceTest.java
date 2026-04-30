package com.rentra.service.rental_agency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalAgencyServiceTest {
    @Mock
    private RentalAgencyRepository rentalAgencyRepository;

    @Mock
    private UserService userService;

    @Mock
    private RentalAgencyMapper rentalAgencyMapper;

    @Mock
    private RentalAgencyUserRepository rentalAgencyUserRepository;

    @InjectMocks
    private RentalAgencyService rentalAgencyService;

    // =========================== create ===========================

    @Test
    void create_shouldPersistAgencyCreateOwnerMembershipAndReturnResponse() {
        UUID ownerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UserEntity owner = createOwner(ownerId);
        CreateRentalAgencyRequest request = new CreateRentalAgencyRequest(
                "Rentra Baku", "Premium city rentals", new BigDecimal("40.4092640"), new BigDecimal("49.8670920"));

        RentalAgencyEntity savedAgency = new RentalAgencyEntity();
        savedAgency.setId(agencyId);
        savedAgency.setOwnerUser(owner);
        savedAgency.setName(request.name());
        savedAgency.setDescription(request.description());
        savedAgency.setLocationLat(request.locationLat());
        savedAgency.setLocationLng(request.locationLng());
        savedAgency.setStatus(RentalAgencyStatus.ACTIVE);

        RentalAgencyResponse expectedResponse = new RentalAgencyResponse(
                agencyId, request.name(), request.description(), null, request.locationLat(), request.locationLng());

        when(userService.findOrThrow(ownerId)).thenReturn(owner);
        when(rentalAgencyRepository.save(any(RentalAgencyEntity.class))).thenReturn(savedAgency);
        when(rentalAgencyMapper.toResponse(savedAgency)).thenReturn(expectedResponse);

        RentalAgencyResponse result = rentalAgencyService.create(request, ownerId);

        assertEquals(expectedResponse, result);

        ArgumentCaptor<RentalAgencyEntity> agencyCaptor = ArgumentCaptor.forClass(RentalAgencyEntity.class);
        verify(rentalAgencyRepository).save(agencyCaptor.capture());
        RentalAgencyEntity persistedAgency = agencyCaptor.getValue();
        assertSame(owner, persistedAgency.getOwnerUser());
        assertEquals(request.name(), persistedAgency.getName());
        assertEquals(request.description(), persistedAgency.getDescription());
        assertEquals(request.locationLat(), persistedAgency.getLocationLat());
        assertEquals(request.locationLng(), persistedAgency.getLocationLng());
        assertEquals(RentalAgencyStatus.ACTIVE, persistedAgency.getStatus());

        ArgumentCaptor<RentalAgencyUserEntity> membershipCaptor = ArgumentCaptor.forClass(RentalAgencyUserEntity.class);
        verify(rentalAgencyUserRepository).save(membershipCaptor.capture());
        RentalAgencyUserEntity membership = membershipCaptor.getValue();
        assertEquals(agencyId, membership.getRentalAgencyId());
        assertEquals(ownerId, membership.getUserId());
        assertEquals(AgencyRole.MANAGER, membership.getRole());
        assertEquals(UserStatus.ACTIVE, membership.getStatus());

        verify(rentalAgencyMapper).toResponse(savedAgency);
    }

    // =========================== findOrThrow ===========================

    @Test
    void findOrThrow_shouldReturnAgency_whenAgencyExists() {
        UUID agencyId = UUID.randomUUID();
        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setId(agencyId);

        when(rentalAgencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));

        RentalAgencyEntity result = rentalAgencyService.findOrThrow(agencyId);

        assertSame(agency, result);
        verify(rentalAgencyRepository).findById(agencyId);
    }

    @Test
    void findOrThrow_shouldThrowResourceNotFoundException_whenAgencyDoesNotExist() {
        UUID agencyId = UUID.randomUUID();
        when(rentalAgencyRepository.findById(agencyId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> rentalAgencyService.findOrThrow(agencyId));

        assertEquals("Rental agency not found for id: " + agencyId, exception.getMessage());
        verify(rentalAgencyRepository).findById(agencyId);
    }

    private UserEntity createOwner(UUID ownerId) {
        UserEntity owner = new UserEntity();
        owner.setId(ownerId);
        owner.setFirstName("Omar");
        owner.setLastName("Ismayilov");
        owner.setEmail("omar.ismayilov@example.com");
        owner.setPhoneNumber("+994 99 999 99 99");
        owner.setBirthDate(LocalDate.of(1995, 5, 15));
        owner.setStatus(UserStatus.ACTIVE);
        owner.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        owner.setUpdatedAt(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        return owner;
    }
}
