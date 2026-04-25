package com.rentra.controller;

import com.rentra.dto.vehicle.VehicleDetailsResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummaryResponse;
import com.rentra.service.vehicle.VehicleServiceImpl;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleServiceImpl vehicleService;
    public VehicleController(VehicleServiceImpl vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{vehicleId}")
    public VehicleDetailsResponse getVehicleById(@PathVariable("vehicleId") UUID vehicleId){
        return vehicleService.getVehicleDetails(vehicleId);
    }

    @GetMapping("/search")
    public List<VehicleSummaryResponse> searchVehicles(@Valid @ModelAttribute VehicleSearchRequest request) {
        return vehicleService.searchVehicles(request);
    }

}
