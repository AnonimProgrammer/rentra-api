package com.rentra.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentra.dto.vehicle.ReservationResponse;
import com.rentra.dto.vehicle.ReserveVehicleRequest;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveVehicle(@Valid @RequestBody ReserveVehicleRequest request) {
        ReservationResponse response = vehicleService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
