package com.rentra.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.ConfirmReservationRequest;
import com.rentra.dto.vehicle.ReservationResponse;
import com.rentra.dto.vehicle.ReserveVehicleRequest;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final VehicleService vehicleService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveVehicle(@Valid @RequestBody ReserveVehicleRequest request) {
        ReservationResponse response = vehicleService.reserve(authService.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<RentResponse> confirmReservation(
            @PathVariable("id") UUID id, @Valid @RequestBody ConfirmReservationRequest request) {
        RentResponse response = vehicleService.confirmReservation(authService.getCurrentUserId(), id, request);
        return ResponseEntity.ok(response);
    }
}
