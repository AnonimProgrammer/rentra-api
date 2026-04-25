package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.domain.rental_service.RentalService;
import com.rentra.domain.vehicle.Vehicle;
import com.rentra.dto.rental_service.RentalServiceRequest;
import com.rentra.dto.rental_service.RentalServiceRespond;
import com.rentra.mapper.RentalServiceMapper;
import com.rentra.service.rental_service.RentalManagementService;

@RestController
@RequestMapping("/v1/services")
public class RentalServiceController {
    private final RentalManagementService rentalManagementService;

    public RentalServiceController(RentalManagementService rentalManagementService) {
        this.rentalManagementService = rentalManagementService;
    }

    @GetMapping
    public List<RentalServiceRespond> findAll() {
        List<RentalService> rentals = rentalManagementService.findAll();
        return RentalServiceMapper.toResponseList(rentals);
    }

    @GetMapping("/{id}")
    public RentalService findById(@PathVariable UUID id) {
        return rentalManagementService.findById(id);
    }

    @GetMapping("/{id}/vehicles")
    public List<Vehicle> findVehiclesByRentalServiceId(@PathVariable UUID id) {
        return rentalManagementService.findVehiclesByRentalServiceId(id);
    }

    @PostMapping
    public ResponseEntity<RentalServiceRespond> create(@RequestBody RentalServiceRequest request) {
        RentalService rentalService = rentalManagementService.create(request);
        RentalServiceRespond respond = RentalServiceMapper.toResponse(rentalService);
        return ResponseEntity.status(HttpStatus.CREATED).body(respond);
    }
}
