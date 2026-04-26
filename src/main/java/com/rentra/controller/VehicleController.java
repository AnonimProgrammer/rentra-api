package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetails;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public VehicleDetails createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.create(request);
    }

    @GetMapping("/{id}")
    public VehicleDetails getVehicleById(@PathVariable("id") UUID id) {
        return vehicleService.getDetails(id);
    }

    @GetMapping("/search")
    public List<VehicleSummary> searchVehicles(@Valid @ModelAttribute VehicleSearchRequest request) {
        return vehicleService.search(request);
    }
}
