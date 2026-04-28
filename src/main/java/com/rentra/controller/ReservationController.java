package com.rentra.controller;

import java.util.UUID;

import com.rentra.service.security.auth.AuthService;
import com.rentra.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.ReservationResponse;
import com.rentra.dto.vehicle.ReserveVehicleRequest;
import com.rentra.service.rent.RentService;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final VehicleService vehicleService;
    private final RentService rentService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveVehicle(@Valid @RequestBody ReserveVehicleRequest request) {
        ReservationResponse response = vehicleService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<RentResponse> confirmRent(@PathVariable("id") UUID id) {
        UUID currentUserId = authService.getCurrentUserId();
        RentResponse response = vehicleService.confirmReservation(id, currentUserId);
        return ResponseEntity.ok(response);
    }
}
