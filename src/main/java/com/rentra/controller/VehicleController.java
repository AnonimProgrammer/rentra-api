package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import com.rentra.dto.vehicle.CreateVehicleRequest;
import org.springframework.web.bind.annotation.*;

import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;
import com.rentra.service.vehicle.VehicleService;
import com.rentra.service.vehicle.VehicleServiceImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleServiceImpl vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/v1/vehicles")
    public VehicleDetailsResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request ) {
        return vehicleService.createVehicle(request);
    }

    @GetMapping("/{vehicleId}")
    public VehicleDetailsResponse getVehicleById(@PathVariable("vehicleId") UUID vehicleId) {
        return vehicleService.getVehicleDetails(vehicleId);
    }

    @GetMapping("/search")
    public List<VehicleSummaryResponse> searchVehicles(@Valid @ModelAttribute VehicleSearchRequest request) {
        return vehicleService.searchVehicles(request);
    }
}
