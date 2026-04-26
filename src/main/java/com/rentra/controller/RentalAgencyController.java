package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.rental_agency.CreateRentalAgencyRequest;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.security.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/agencies")
@RequiredArgsConstructor
public class RentalAgencyController {
    private final RentalAgencyService rentalAgencyService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<RentalAgencyResponse> create(@Valid @RequestBody CreateRentalAgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalAgencyService.create(request, authService.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalAgencyResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(rentalAgencyService.getById(id));
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<VehicleSummary>> findVehiclesByRentalAgencyId(@PathVariable UUID id) {
        return ResponseEntity.ok(rentalAgencyService.findVehiclesByAgency(id));
    }
}
