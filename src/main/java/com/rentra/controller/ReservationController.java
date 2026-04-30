package com.rentra.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.reservation.CreateReservationRequest;
import com.rentra.dto.reservation.ReservationResponse;
import com.rentra.service.reservation.ReservationService;
import com.rentra.service.security.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveVehicle(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.reserve(authService.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<RentResponse> confirm(@PathVariable("id") UUID id) {
        RentResponse response = reservationService.confirm(authService.getCurrentUserId(), id);
        return ResponseEntity.ok(response);
    }
}
