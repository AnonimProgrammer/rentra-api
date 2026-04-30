package com.rentra.service.reservation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.reservation.ReservationEntity;
import com.rentra.domain.reservation.ReservationStatus;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.reservation.CreateReservationRequest;
import com.rentra.dto.reservation.ReservationResponse;
import com.rentra.dto.reservation.ReservationSearchRequest;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.mapper.ReservationMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.reservation.ReservationRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.rent.RentService;
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
class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RentRepository rentRepository;

    @Mock
    private RentService rentService;

    @Mock
    private RentMapper rentMapper;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private UserService userService;

    @Mock
    private AgencyAuthService agencyAuthService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private ReservationService reservationService;

    // =========================== reserve ===========================

    @Test
    void reserve_shouldCreateReservationAndMarkVehicleReserved_whenPreconditionsPass() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        UserEntity customer = createUser(customerId);
        RentalAgencyEntity agency = createAgency(agencyId, customer);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.AVAILABLE);
        CreateReservationRequest request = new CreateReservationRequest(vehicleId);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(reservationId);
        savedReservation.setCustomer(customer);
        savedReservation.setVehicle(vehicle);
        savedReservation.setRentalAgency(agency);
        savedReservation.setStatus(ReservationStatus.RESERVED);

        ReservationResponse expected = new ReservationResponse(
                reservationId,
                customerId,
                vehicleId,
                agencyId,
                ReservationStatus.RESERVED,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                null,
                null,
                OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        when(vehicleService.findOrThrow(vehicleId)).thenReturn(vehicle);
        when(rentRepository.existsByCustomerIdAndStatus(customerId, RentStatus.ACTIVE))
                .thenReturn(false);
        when(reservationRepository.existsByCustomerIdAndStatus(customerId, ReservationStatus.RESERVED))
                .thenReturn(false);
        when(userService.findOrThrow(customerId)).thenReturn(customer);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);
        when(reservationMapper.toResponse(savedReservation)).thenReturn(expected);

        ReservationResponse result = reservationService.reserve(customerId, request);

        assertEquals(expected, result);
        assertEquals(VehicleStatus.RESERVED, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);

        ArgumentCaptor<ReservationEntity> reservationCaptor = ArgumentCaptor.forClass(ReservationEntity.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        ReservationEntity persisted = reservationCaptor.getValue();
        assertSame(customer, persisted.getCustomer());
        assertSame(vehicle, persisted.getVehicle());
        assertSame(agency, persisted.getRentalAgency());
        assertEquals(ReservationStatus.RESERVED, persisted.getStatus());
    }

    @Test
    void reserve_shouldThrowConflictException_whenCustomerAlreadyHasActiveRent() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CreateReservationRequest request = new CreateReservationRequest(vehicleId);

        when(vehicleService.findOrThrow(vehicleId)).thenReturn(new VehicleEntity());
        when(rentRepository.existsByCustomerIdAndStatus(customerId, RentStatus.ACTIVE))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(ConflictException.class, () -> reservationService.reserve(customerId, request));

        assertEquals("Customer already has an active rent. Cannot create a new reservation.", exception.getMessage());
    }

    @Test
    void reserve_shouldThrowConflictException_whenVehicleIsNotAvailable() {
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VehicleEntity vehicle = createVehicle(vehicleId, new RentalAgencyEntity(), VehicleStatus.RENTED);
        CreateReservationRequest request = new CreateReservationRequest(vehicleId);

        when(vehicleService.findOrThrow(vehicleId)).thenReturn(vehicle);
        when(rentRepository.existsByCustomerIdAndStatus(customerId, RentStatus.ACTIVE))
                .thenReturn(false);
        when(reservationRepository.existsByCustomerIdAndStatus(customerId, ReservationStatus.RESERVED))
                .thenReturn(false);

        ConflictException exception =
                assertThrows(ConflictException.class, () -> reservationService.reserve(customerId, request));

        assertEquals("Vehicle is not available", exception.getMessage());
    }

    // =========================== confirm ===========================

    @Test
    void confirm_shouldExpireReservationAndThrowConflict_whenReservationExpired() {
        UUID agencyUserId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        UserEntity agencyUser = createUser(agencyUserId);
        RentalAgencyEntity agency = createAgency(agencyId, createUser(UUID.randomUUID()));
        VehicleEntity vehicle = createVehicle(UUID.randomUUID(), agency, VehicleStatus.RESERVED);
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(reservationId);
        reservation.setVehicle(vehicle);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

        when(userService.findOrThrow(agencyUserId)).thenReturn(agencyUser);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        ConflictException exception =
                assertThrows(ConflictException.class, () -> reservationService.confirm(agencyUserId, reservationId));

        assertEquals("Reservation has expired", exception.getMessage());
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void confirm_shouldCreateRentAndConfirmReservation_whenReservationValid() {
        UUID agencyUserId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        UserEntity agencyUser = createUser(agencyUserId);
        UserEntity customer = createUser(customerId);
        RentalAgencyEntity agency = createAgency(agencyId, createUser(UUID.randomUUID()));
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.RESERVED);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(reservationId);
        reservation.setCustomer(customer);
        reservation.setVehicle(vehicle);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiresAt(OffsetDateTime.now().plusHours(1));

        RentEntity savedRent = new RentEntity();
        savedRent.setId(UUID.randomUUID());
        RentResponse expected = new RentResponse(
                savedRent.getId(), customerId, vehicleId, agencyId, null, null, RentStatus.ACTIVE, null, null);

        when(userService.findOrThrow(agencyUserId)).thenReturn(agencyUser);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(rentService.create(customer, vehicle)).thenReturn(savedRent);
        when(rentMapper.toResponse(savedRent)).thenReturn(expected);

        RentResponse result = reservationService.confirm(agencyUserId, reservationId);

        assertEquals(expected, result);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
        verify(reservationRepository).save(reservation);
    }

    // =========================== getByAgencyId ===========================

    @Test
    void getByAgencyId_shouldReturnMappedPageWithPagination() {
        UUID requesterId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        UserEntity requester = createUser(requesterId);

        List<ReservationEntity> reservations = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            ReservationEntity entity = new ReservationEntity();
            entity.setId(UUID.randomUUID());
            reservations.add(entity);
        }

        ReservationSearchRequest request =
                new ReservationSearchRequest(null, null, ReservationStatus.RESERVED, cursor, 50);

        when(userService.findOrThrow(requesterId)).thenReturn(requester);
        when(reservationRepository.findAgencyReservations(agencyId, null, null, "RESERVED", cursor, 21))
                .thenReturn(reservations);

        for (int i = 0; i < 20; i++) {
            ReservationEntity entity = reservations.get(i);
            when(reservationMapper.toResponse(entity))
                    .thenReturn(new ReservationResponse(
                            entity.getId(), null, null, agencyId, ReservationStatus.RESERVED, null, null, null, null));
        }

        PageResponse<ReservationResponse> result = reservationService.getByAgencyId(requesterId, agencyId, request);

        assertEquals(20, result.data().size());
        assertEquals(20, result.pagination().limit());
        assertEquals(
                reservations.get(19).getId().toString(), result.pagination().nextCursor());
        assertEquals(cursor.toString(), result.pagination().previousCursor());
        assertEquals(true, result.pagination().hasNext());
        assertEquals(true, result.pagination().hasPrevious());
    }

    // =========================== findOrThrow ===========================

    @Test
    void findOrThrow_shouldReturnReservation_whenReservationExists() {
        UUID reservationId = UUID.randomUUID();
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        ReservationEntity result = reservationService.findOrThrow(reservationId);

        assertSame(reservation, result);
    }

    @Test
    void findOrThrow_shouldThrowResourceNotFoundException_whenReservationDoesNotExist() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> reservationService.findOrThrow(reservationId));

        assertEquals("Reservation not found for id: " + reservationId, exception.getMessage());
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
}
