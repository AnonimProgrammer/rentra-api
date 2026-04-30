package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rental_agency.AgencyMembershipResponse;
import com.rentra.dto.rental_agency.ConfirmJoinRequest;
import com.rentra.dto.rental_agency.ConfirmJoinResponse;
import com.rentra.dto.rental_agency.CreateRentalAgencyRequest;
import com.rentra.dto.rental_agency.MyAgencyMembershipResponse;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.dto.rental_agency.RequestJoinResponse;
import com.rentra.dto.rental_agency.UpdateAgencyMembership;
import com.rentra.dto.reservation.ReservationResponse;
import com.rentra.dto.reservation.ReservationSearchRequest;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.service.rental_agency.AgencyMembershipService;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.reservation.ReservationService;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/agencies")
@RequiredArgsConstructor
public class RentalAgencyController {
    private final RentalAgencyService rentalAgencyService;
    private final AgencyMembershipService agencyMembershipService;
    private final AgencyAuthService agencyAuthService;
    private final AuthService authService;
    private final VehicleService vehicleService;
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<RentalAgencyResponse> create(@Valid @RequestBody CreateRentalAgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalAgencyService.create(request, authService.getCurrentUserId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyAgencyMembershipResponse>> getMyMemberships() {
        return ResponseEntity.ok(agencyMembershipService.getMyMemberships(authService.getCurrentUserId()));
    }

    // Add admin endpoint to get all rental agencies with pagination and filters

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<PageResponse<VehicleSummary>> getAgencyVehicles(
            @PathVariable UUID id, @Valid @ModelAttribute VehicleSearchRequest request) {
        return ResponseEntity.ok(vehicleService.getByAgencyId(authService.getCurrentUserId(), id, request));
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<PageResponse<ReservationResponse>> getAgencyReservations(
            @PathVariable UUID id, @Valid @ModelAttribute ReservationSearchRequest request) {
        return ResponseEntity.ok(reservationService.getByAgencyId(authService.getCurrentUserId(), id, request));
    }

    @GetMapping("/{id}/memberships")
    public ResponseEntity<PageResponse<AgencyMembershipResponse>> getMemberships(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) AgencyRole role,
            @RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(agencyMembershipService.getMemberships(
                id, authService.getCurrentUserId(), cursor, limit, role, status));
    }

    @PostMapping("/{id}/join-requests")
    public ResponseEntity<RequestJoinResponse> requestJoin(@PathVariable UUID id) {
        RequestJoinResponse response = agencyAuthService.requestAuthorization(id, authService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/join-requests/confirm")
    public ResponseEntity<ConfirmJoinResponse> confirmJoin(
            @PathVariable UUID id, @Valid @RequestBody ConfirmJoinRequest request) {
        ConfirmJoinResponse response =
                agencyAuthService.confirmAuthorization(id, authService.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/memberships")
    public ResponseEntity<AgencyMembershipResponse> updateMembership(
            @PathVariable UUID id, @Valid @RequestBody UpdateAgencyMembership request) {
        AgencyMembershipResponse response =
                agencyMembershipService.updateMembership(id, authService.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }
}
