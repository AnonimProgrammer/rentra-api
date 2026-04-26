package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.dto.rental_agency.RentalAgencyRequest;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.mapper.RentalAgencyMapper;
import com.rentra.service.rental_agency.RentalAgencyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/agencies")
@RequiredArgsConstructor
public class RentalAgencyController {
    private final RentalAgencyService rentalAgencyService;

    @GetMapping
    public List<RentalAgencyResponse> findAll() {
        List<RentalAgencyEntity> agencies = rentalAgencyService.findAll();
        return RentalAgencyMapper.toResponseList(agencies);
    }

    @GetMapping("/{id}")
    public RentalAgencyEntity findById(@PathVariable UUID id) {
        return rentalAgencyService.findById(id);
    }

    @GetMapping("/{id}/vehicles")
    public List<VehicleEntity> findVehiclesByRentalAgencyId(@PathVariable UUID id) {
        return rentalAgencyService.findVehiclesByRentalAgencyId(id);
    }

    @PostMapping
    public ResponseEntity<RentalAgencyResponse> create(@RequestBody RentalAgencyRequest request) {
        RentalAgencyEntity rentalAgency = rentalAgencyService.create(request);
        RentalAgencyResponse response = RentalAgencyMapper.toResponse(rentalAgency);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
