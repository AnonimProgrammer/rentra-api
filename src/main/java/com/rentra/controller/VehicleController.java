package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetails;
import com.rentra.dto.vehicle.VehicleRentHistoryRequest;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.service.rent.RentService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;
    private final RentService rentService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<VehicleDetails> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.ok(vehicleService.create(authService.getCurrentUserId(), request));
    }

    @PostMapping("/{id}/technical-check/complete")
    public ResponseEntity<VehicleSummary> completeTechnicalCheck(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vehicleService.completeTechnicalCheck(id, authService.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDetails> getVehicleById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vehicleService.getDetails(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleSummary>> searchVehicles(@Valid @ModelAttribute VehicleSearchRequest request) {
        return ResponseEntity.ok(vehicleService.search(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<VehicleSummary>> getAllVehicles(
            @Valid @ModelAttribute VehicleSearchRequest request) {
        return ResponseEntity.ok(vehicleService.getAll(request));
    }

    @GetMapping("/{id}/rents/history")
    public ResponseEntity<PageResponse<RentResponse>> getVehicleRentHistory(
            @PathVariable("id") UUID id, @Valid @ModelAttribute VehicleRentHistoryRequest request) {
        return ResponseEntity.ok(rentService.getRentHistoryByVehicleId(authService.getCurrentUserId(), id, request));
    }
}
