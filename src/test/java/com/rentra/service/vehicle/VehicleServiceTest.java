package com.rentra.service.vehicle;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.vehicle.CreateVehicleRateRequest;
import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.UpdateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetails;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.VehicleMapper;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RentalAgencyService rentalAgencyService;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private UserService userService;

    @Mock
    private AgencyAuthService agencyAuthService;

    @InjectMocks
    private VehicleService vehicleService;

    // =========================== create ===========================

    @Test
    void create_shouldSetAvailableStatus_whenRatesExist() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        RentalAgencyEntity agency = createAgency(agencyId, user);

        CreateVehicleRequest request = new CreateVehicleRequest(
                agencyId,
                VehicleCategory.SUV,
                "Toyota",
                "RAV4",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                List.of(new CreateVehicleRateRequest(
                        com.rentra.domain.vehicle.RateType.DAY,
                        java.math.BigDecimal.valueOf(100),
                        com.rentra.domain.payment.Currency.AZN)));

        VehicleEntity mapped = createVehicle(vehicleId, agency, VehicleStatus.INCOMPLETE);
        VehicleEntity saved = createVehicle(vehicleId, agency, VehicleStatus.AVAILABLE);
        VehicleDetails expected = new VehicleDetails(
                vehicleId,
                null,
                VehicleCategory.SUV,
                "Toyota",
                "RAV4",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE,
                List.of());

        when(userService.findOrThrow(userId)).thenReturn(user);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(agency);
        when(vehicleMapper.toEntity(request, agency)).thenReturn(mapped);
        when(vehicleRepository.save(mapped)).thenReturn(saved);
        when(vehicleMapper.toDetails(saved)).thenReturn(expected);

        VehicleDetails result = vehicleService.create(userId, request);

        assertEquals(expected, result);
        assertEquals(VehicleStatus.AVAILABLE, mapped.getStatus());
    }

    @Test
    void create_shouldSetIncompleteStatus_whenRatesAreMissing() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        RentalAgencyEntity agency = createAgency(agencyId, user);
        CreateVehicleRequest request = new CreateVehicleRequest(
                agencyId,
                VehicleCategory.SEDAN,
                "Kia",
                "K5",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                List.of());

        VehicleEntity mapped = createVehicle(UUID.randomUUID(), agency, VehicleStatus.AVAILABLE);
        when(userService.findOrThrow(userId)).thenReturn(user);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(agency);
        when(vehicleMapper.toEntity(request, agency)).thenReturn(mapped);
        when(vehicleRepository.save(mapped)).thenReturn(mapped);
        when(vehicleMapper.toDetails(mapped))
                .thenReturn(new VehicleDetails(
                        mapped.getId(),
                        null,
                        VehicleCategory.SEDAN,
                        "Kia",
                        "K5",
                        TransmissionType.AUTOMATIC,
                        FuelType.PETROL,
                        5,
                        VehicleStatus.INCOMPLETE,
                        List.of()));

        vehicleService.create(userId, request);

        assertEquals(VehicleStatus.INCOMPLETE, mapped.getStatus());
    }

    // =========================== update ===========================

    @Test
    void update_shouldApplyFieldsAndSwitchToAvailable_whenRatesProvidedAndStatusIncomplete() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        RentalAgencyEntity agency = createAgency(agencyId, user);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.INCOMPLETE);
        vehicle.setRates(new ArrayList<>());

        CreateVehicleRateRequest rateRequest = new CreateVehicleRateRequest(
                com.rentra.domain.vehicle.RateType.HOUR,
                java.math.BigDecimal.valueOf(15),
                com.rentra.domain.payment.Currency.AZN);
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                VehicleCategory.SUV,
                "Hyundai",
                "Tucson",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                List.of(rateRequest));

        VehicleRateEntity mappedRate = new VehicleRateEntity();
        VehicleDetails expected = new VehicleDetails(
                vehicleId,
                null,
                VehicleCategory.SUV,
                "Hyundai",
                "Tucson",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE,
                List.of());

        when(userService.findOrThrow(userId)).thenReturn(user);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toRateEntity(rateRequest)).thenReturn(mappedRate);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);
        when(vehicleMapper.toDetails(vehicle)).thenReturn(expected);

        VehicleDetails result = vehicleService.update(userId, vehicleId, request);

        assertEquals(expected, result);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(1, vehicle.getRates().size());
        assertSame(vehicle, vehicle.getRates().getFirst().getVehicle());
    }

    // =========================== completeTechnicalCheck ===========================

    @Test
    void completeTechnicalCheck_shouldSetAvailable_whenVehicleInTechnicalCheck() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        RentalAgencyEntity agency = createAgency(agencyId, user);
        VehicleEntity vehicle = createVehicle(vehicleId, agency, VehicleStatus.TECHNICAL_CHECK);
        VehicleSummary expected = new VehicleSummary(
                vehicleId,
                agencyId,
                "BMW",
                "X5",
                VehicleCategory.SUV,
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE);

        when(userService.findOrThrow(userId)).thenReturn(user);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);
        when(vehicleMapper.toSummary(vehicle)).thenReturn(expected);

        VehicleSummary result = vehicleService.completeTechnicalCheck(userId, vehicleId);

        assertEquals(expected, result);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }

    @Test
    void completeTechnicalCheck_shouldThrowConflictException_whenVehicleNotInTechnicalCheck() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        VehicleEntity vehicle = createVehicle(vehicleId, new RentalAgencyEntity(), VehicleStatus.AVAILABLE);

        when(userService.findOrThrow(userId)).thenReturn(user);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        ConflictException exception =
                assertThrows(ConflictException.class, () -> vehicleService.completeTechnicalCheck(userId, vehicleId));

        assertEquals("Vehicle must be in TECHNICAL_CHECK state", exception.getMessage());
    }

    // =========================== search/getAll/getByAgencyId ===========================

    @Test
    void search_shouldEnforceAvailableStatus_andClampLimit() {
        UUID agencyId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        VehicleSearchRequest request = new VehicleSearchRequest(
                agencyId,
                VehicleCategory.SUV,
                "bm",
                "x",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.RENTED,
                cursor,
                99);

        List<VehicleEntity> vehicles = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            VehicleEntity vehicle = new VehicleEntity();
            vehicle.setId(UUID.randomUUID());
            vehicles.add(vehicle);
        }

        when(vehicleRepository.findVehicles(
                        agencyId, "SUV", "bm", "x", "AUTOMATIC", "PETROL", 5, "AVAILABLE", cursor, 21))
                .thenReturn(vehicles);
        for (int i = 0; i < 20; i++) {
            when(vehicleMapper.toSummary(vehicles.get(i)))
                    .thenReturn(new VehicleSummary(
                            vehicles.get(i).getId(),
                            agencyId,
                            "B",
                            "M",
                            VehicleCategory.SUV,
                            TransmissionType.AUTOMATIC,
                            FuelType.PETROL,
                            5,
                            VehicleStatus.AVAILABLE));
        }

        PageResponse<VehicleSummary> result = vehicleService.search(request);

        assertEquals(20, result.data().size());
        assertEquals(20, result.pagination().limit());
        assertEquals(vehicles.get(19).getId().toString(), result.pagination().nextCursor());
    }

    @Test
    void getByAgencyId_shouldAuthorizeAndReturnPagedData() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        VehicleSearchRequest request =
                new VehicleSearchRequest(null, null, null, null, null, null, null, VehicleStatus.AVAILABLE, null, null);

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(UUID.randomUUID());
        when(userService.findOrThrow(userId)).thenReturn(user);
        when(rentalAgencyService.findOrThrow(agencyId)).thenReturn(new RentalAgencyEntity());
        when(vehicleRepository.findVehicles(agencyId, null, null, null, null, null, null, "AVAILABLE", null, 21))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toSummary(vehicle))
                .thenReturn(new VehicleSummary(
                        vehicle.getId(),
                        agencyId,
                        "Audi",
                        "A6",
                        VehicleCategory.SEDAN,
                        TransmissionType.AUTOMATIC,
                        FuelType.PETROL,
                        5,
                        VehicleStatus.AVAILABLE));

        PageResponse<VehicleSummary> result = vehicleService.getByAgencyId(userId, agencyId, request);

        assertEquals(1, result.data().size());
        verify(agencyAuthService)
                .verifyAuthority(
                        user,
                        agencyId,
                        List.of(
                                com.rentra.domain.rental_agency.AgencyRole.FRONT_AGENT,
                                com.rentra.domain.rental_agency.AgencyRole.MANAGER));
    }

    // =========================== getDetails/findOrThrow ===========================

    @Test
    void getDetails_shouldMapVehicleToDetails() {
        UUID vehicleId = UUID.randomUUID();
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(vehicleId);
        VehicleDetails expected = new VehicleDetails(
                vehicleId,
                null,
                VehicleCategory.SUV,
                "BMW",
                "X5",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE,
                List.of());

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toDetails(vehicle)).thenReturn(expected);

        VehicleDetails result = vehicleService.getDetails(vehicleId);

        assertEquals(expected, result);
    }

    @Test
    void findOrThrow_shouldThrowResourceNotFoundException_whenVehicleMissing() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> vehicleService.findOrThrow(vehicleId));

        assertEquals("Vehicle not found for id: " + vehicleId, exception.getMessage());
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
        vehicle.setCategory(VehicleCategory.SUV);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setTransmission(TransmissionType.AUTOMATIC);
        vehicle.setFuelType(FuelType.PETROL);
        vehicle.setSeatCount(5);
        vehicle.setStatus(status);
        return vehicle;
    }
}
