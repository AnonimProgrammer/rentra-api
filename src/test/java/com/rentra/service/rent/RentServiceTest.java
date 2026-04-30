package com.rentra.service.rent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.VehicleRentHistoryRequest;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.price.PriceEngine;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import com.rentra.service.vehicle.VehicleService;
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
class RentServiceTest {
    @Mock
    private RentRepository rentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PriceEngine priceEngine;

    @Mock
    private RentMapper rentMapper;

    @Mock
    private UserService userService;

    @Mock
    private AgencyAuthService agencyAuthService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private RentService rentService;

    // =========================== complete ===========================

    @Test
    void complete_shouldCompleteRentAndSetVehicleTechnicalCheck_whenActiveAndAuthorized() {
        UUID rentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        UserEntity customer = createUser(customerId);
        RentalAgencyEntity agency = createAgency(agencyId, customer);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.RENTED);
        RentEntity rent = createRent(rentId, customer, vehicle, RentStatus.ACTIVE);
        BigDecimal finalAmount = new BigDecimal("123.45");
        RentResponse expected = new RentResponse(
                rentId,
                customerId,
                vehicleId,
                agencyId,
                finalAmount,
                null,
                RentStatus.COMPLETED,
                rent.getStartsAt(),
                null);

        when(rentRepository.findById(rentId)).thenReturn(Optional.of(rent));
        when(priceEngine.calculateFinalAmount(rent)).thenReturn(finalAmount);
        when(rentRepository.save(rent)).thenReturn(rent);
        when(rentMapper.toResponse(rent)).thenReturn(expected);

        RentResponse result = rentService.complete(rentId, customerId);

        assertEquals(expected, result);
        assertEquals(RentStatus.COMPLETED, rent.getStatus());
        assertEquals(finalAmount, rent.getTotalAmount());
        assertEquals(VehicleStatus.TECHNICAL_CHECK, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
        verify(rentRepository).save(rent);
    }

    @Test
    void complete_shouldThrowAuthorizationDeniedException_whenCustomerIsNotOwner() {
        UUID rentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID anotherCustomerId = UUID.randomUUID();

        UserEntity owner = createUser(customerId);
        VehicleEntity vehicle = createVehicle(UUID.randomUUID(), new RentalAgencyEntity(), VehicleStatus.RENTED);
        RentEntity rent = createRent(rentId, owner, vehicle, RentStatus.ACTIVE);
        when(rentRepository.findById(rentId)).thenReturn(Optional.of(rent));

        AuthorizationDeniedException exception =
                assertThrows(AuthorizationDeniedException.class, () -> rentService.complete(rentId, anotherCustomerId));

        assertEquals("Access denied for rent operation", exception.getMessage());
    }

    // =========================== rate ===========================

    @Test
    void rate_shouldSetRatingAndReturnMappedResponse_whenCompletedAndAuthorized() {
        UUID rentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Integer rating = 5;

        UserEntity customer = createUser(customerId);
        VehicleEntity vehicle =
                createVehicle(UUID.randomUUID(), new RentalAgencyEntity(), VehicleStatus.TECHNICAL_CHECK);
        RentEntity rent = createRent(rentId, customer, vehicle, RentStatus.COMPLETED);
        RentResponse expected = new RentResponse(
                rentId,
                customerId,
                vehicle.getId(),
                null,
                rent.getTotalAmount(),
                rating,
                RentStatus.COMPLETED,
                null,
                null);

        when(rentRepository.findById(rentId)).thenReturn(Optional.of(rent));
        when(rentRepository.save(rent)).thenReturn(rent);
        when(rentMapper.toResponse(rent)).thenReturn(expected);

        RentResponse result = rentService.rate(rentId, customerId, rating);

        assertEquals(expected, result);
        assertEquals(rating, rent.getRating());
        verify(rentRepository).save(rent);
    }

    @Test
    void rate_shouldThrowIllegalArgumentException_whenRentIsNotCompleted() {
        UUID rentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        UserEntity customer = createUser(customerId);
        VehicleEntity vehicle = createVehicle(UUID.randomUUID(), new RentalAgencyEntity(), VehicleStatus.RENTED);
        RentEntity rent = createRent(rentId, customer, vehicle, RentStatus.ACTIVE);
        when(rentRepository.findById(rentId)).thenReturn(Optional.of(rent));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> rentService.rate(rentId, customerId, 4));

        assertEquals("Rating is only allowed for COMPLETED rent", exception.getMessage());
    }

    // =========================== create ===========================

    @Test
    void create_shouldBuildActiveRentWithZeroAmountAndPersist() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();

        UserEntity customer = createUser(customerId);
        RentalAgencyEntity agency = createAgency(agencyId, customer);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.RESERVED);

        RentEntity saved = new RentEntity();
        saved.setId(rentId);
        when(rentRepository.save(any(RentEntity.class))).thenReturn(saved);

        RentEntity result = rentService.create(customer, vehicle);

        assertSame(saved, result);
        ArgumentCaptor<RentEntity> captor = ArgumentCaptor.forClass(RentEntity.class);
        verify(rentRepository).save(captor.capture());
        RentEntity persisted = captor.getValue();
        assertSame(customer, persisted.getCustomer());
        assertSame(vehicle, persisted.getVehicle());
        assertSame(agency, persisted.getRentalAgency());
        assertEquals(RentStatus.ACTIVE, persisted.getStatus());
        assertEquals(BigDecimal.ZERO, persisted.getTotalAmount());
    }

    // =========================== getMyActive ===========================

    @Test
    void getMyActive_shouldReturnMappedResponse_whenActiveRentExists() {
        UUID userId = UUID.randomUUID();
        RentEntity rent = new RentEntity();
        RentResponse expected =
                new RentResponse(UUID.randomUUID(), userId, null, null, null, null, RentStatus.ACTIVE, null, null);

        when(rentRepository.findFirstByCustomerIdAndStatusOrderByIdDesc(userId, RentStatus.ACTIVE))
                .thenReturn(Optional.of(rent));
        when(rentMapper.toResponse(rent)).thenReturn(expected);

        RentResponse result = rentService.getMyActive(userId);

        assertEquals(expected, result);
    }

    @Test
    void getMyActive_shouldThrowResourceNotFoundException_whenNoActiveRentExists() {
        UUID userId = UUID.randomUUID();
        when(rentRepository.findFirstByCustomerIdAndStatusOrderByIdDesc(userId, RentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> rentService.getMyActive(userId));

        assertEquals("User does not have an active rent", exception.getMessage());
    }

    // =========================== findOrThrow ===========================

    @Test
    void findOrThrow_shouldReturnRent_whenRentExists() {
        UUID rentId = UUID.randomUUID();
        RentEntity rent = new RentEntity();
        rent.setId(rentId);
        when(rentRepository.findById(rentId)).thenReturn(Optional.of(rent));

        RentEntity result = rentService.findOrThrow(rentId);

        assertSame(rent, result);
    }

    @Test
    void findOrThrow_shouldThrowResourceNotFoundException_whenRentDoesNotExist() {
        UUID rentId = UUID.randomUUID();
        when(rentRepository.findById(rentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> rentService.findOrThrow(rentId));

        assertEquals("Rent not found for id: " + rentId, exception.getMessage());
    }

    // =========================== getMyRents ===========================

    @Test
    void getMyRents_shouldReturnPagedMappedRents_andClampLimitToDefaultMaximum() {
        UUID userId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();

        List<RentEntity> rents = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            RentEntity rent = new RentEntity();
            rent.setId(UUID.randomUUID());
            rents.add(rent);
        }

        when(rentRepository.findRents(
                        userId, null, "ACTIVE", null, null, null, null, null, null, null, null, cursor, 21))
                .thenReturn(rents);
        for (int i = 0; i < 20; i++) {
            when(rentMapper.toResponse(rents.get(i)))
                    .thenReturn(new RentResponse(
                            rents.get(i).getId(), userId, null, null, null, null, RentStatus.ACTIVE, null, null));
        }

        PageResponse<RentResponse> result = rentService.getMyRents(userId, cursor, 50, RentStatus.ACTIVE);

        assertEquals(20, result.data().size());
        assertEquals(20, result.pagination().limit());
        assertEquals(rents.get(19).getId().toString(), result.pagination().nextCursor());
        assertEquals(cursor.toString(), result.pagination().previousCursor());
        assertEquals(true, result.pagination().hasNext());
        assertEquals(true, result.pagination().hasPrevious());
    }

    // =========================== getRentHistoryByVehicleId ===========================

    @Test
    void getRentHistoryByVehicleId_shouldThrowIllegalArgumentException_whenStartedFromAfterStartedTo() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UserEntity requester = createUser(userId);
        RentalAgencyEntity agency = createAgency(UUID.randomUUID(), requester);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.RENTED);
        VehicleRentHistoryRequest request = new VehicleRentHistoryRequest(
                null,
                10,
                null,
                OffsetDateTime.parse("2026-01-03T10:00:00Z"),
                OffsetDateTime.parse("2026-01-02T10:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null);

        when(userService.findOrThrow(userId)).thenReturn(requester);
        when(vehicleService.findOrThrow(vehicleId)).thenReturn(vehicle);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rentService.getRentHistoryByVehicleId(userId, vehicleId, request));

        assertEquals("startedFrom must be earlier than or equal to startedTo", exception.getMessage());
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

    private RentalAgencyEntity createAgency(UUID agencyId, UserEntity owner) {
        RentalAgencyEntity agency = new RentalAgencyEntity();
        agency.setId(agencyId);
        agency.setOwnerUser(owner);
        return agency;
    }

    private VehicleEntity createVehicle(UUID vehicleId, RentalAgencyEntity agency, VehicleStatus status) {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(vehicleId);
        vehicle.setRentalAgency(agency);
        vehicle.setStatus(status);
        return vehicle;
    }

    private RentEntity createRent(UUID rentId, UserEntity customer, VehicleEntity vehicle, RentStatus status) {
        RentEntity rent = new RentEntity();
        rent.setId(rentId);
        rent.setCustomer(customer);
        rent.setVehicle(vehicle);
        rent.setRentalAgency(vehicle.getRentalAgency());
        rent.setStatus(status);
        rent.setTotalAmount(BigDecimal.ZERO);
        rent.setStartsAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        return rent;
    }
}
