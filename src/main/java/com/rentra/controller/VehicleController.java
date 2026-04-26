package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public VehicleDetailsResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.createVehicle(request);
    }

    @GetMapping("/{id}")
    public VehicleDetailsResponse getVehicleById(@PathVariable("id") UUID id) {
        return vehicleService.getVehicleDetails(id);
    }

    @GetMapping("/search")
    public List<VehicleSummaryResponse> searchVehicles(@Valid @ModelAttribute VehicleSearchRequest request) {
        return vehicleService.searchVehicles(request);
    }
}
