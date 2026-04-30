package com.rentra.service.reservation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.rent.RentEntity;
import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.reservation.ReservationEntity;
import com.rentra.domain.reservation.ReservationStatus;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.reservation.CreateReservationRequest;
import com.rentra.dto.reservation.ReservationResponse;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.RentMapper;
import com.rentra.repository.rent.RentRepository;
import com.rentra.repository.reservation.ReservationRepository;
import com.rentra.repository.vehicle.VehicleRepository;
import com.rentra.service.rent.RentService;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.user.UserService;
import com.rentra.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final RentRepository rentRepository;
    private final RentService rentService;
    private final RentMapper rentMapper;
    private final UserService userService;
    private final AgencyAuthService agencyAuthService;
    private final VehicleService vehicleService;

    @Transactional
    public ReservationResponse reserve(UUID customerId, CreateReservationRequest request) {
        VehicleEntity vehicle = vehicleService.findOrThrow(request.vehicleId());

        boolean hasActiveRent = rentRepository.existsByCustomerIdAndStatus(customerId, RentStatus.ACTIVE);
        if (hasActiveRent) {
            throw new ConflictException("Customer already has an active rent. Cannot create a new reservation.");
        }

        boolean hasActiveReservation =
                reservationRepository.existsByCustomerIdAndStatus(customerId, ReservationStatus.RESERVED);
        if (hasActiveReservation) {
            throw new ConflictException("Customer already has an active reservation.");
        }

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new ConflictException("Vehicle is not available");
        }

        UserEntity customer = userService.findOrThrow(customerId);
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCustomer(customer);
        reservation.setVehicle(vehicle);
        reservation.setRentalAgency(vehicle.getRentalAgency());
        reservation.setStatus(ReservationStatus.RESERVED);
        OffsetDateTime reservedAt = OffsetDateTime.now();
        reservation.setReservedAt(reservedAt);
        reservation.setExpiresAt(reservedAt.plusHours(24));

        vehicle.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(vehicle);
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        return new ReservationResponse(
                savedReservation.getId(),
                savedReservation.getCustomer().getId(),
                savedReservation.getVehicle().getId(),
                savedReservation.getRentalAgency().getId(),
                savedReservation.getStatus(),
                savedReservation.getReservedAt(),
                savedReservation.getConfirmedAt(),
                savedReservation.getCancelledAt(),
                savedReservation.getExpiresAt());
    }

    @Transactional
    public RentResponse confirm(UUID agencyUserId, UUID reservationId) {
        UserEntity agencyUser = userService.findOrThrow(agencyUserId);
        ReservationEntity reservation = findOrThrow(reservationId);

        agencyAuthService.verifyAuthority(
                agencyUser,
                reservation.getVehicle().getRentalAgency().getId(),
                List.of(AgencyRole.MANAGER, AgencyRole.FRONT_AGENT));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new ConflictException("Reservation is not in RESERVED status");
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = reservation.getExpiresAt();
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservation.getVehicle().setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(reservation.getVehicle());
            reservationRepository.save(reservation);
            throw new ConflictException("Reservation has expired");
        }

        RentEntity savedRent = rentService.create(reservation.getCustomer(), reservation.getVehicle());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(OffsetDateTime.now());
        reservation.getVehicle().setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(reservation.getVehicle());
        reservationRepository.save(reservation);

        return rentMapper.toResponse(savedRent);
    }

    public ReservationEntity findOrThrow(UUID reservationId) {
        return reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for id: " + reservationId));
    }
}
