package com.rentra.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentra.dto.vehicle.CreateReservationRequest;
import com.rentra.dto.vehicle.ReservationResponse;
import com.rentra.service.vehicle.VehicleServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final VehicleServiceImpl vehicleService;

    @PostMapping
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return vehicleService.createReservation(request);
    }
}
