package com.rentra.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentra.dto.rent.RateRentRequest;
import com.rentra.dto.rent.RentResponse;
import com.rentra.service.rent.RentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/rents")
@RequiredArgsConstructor
public class RentController {
    private final RentService rentService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<RentResponse> complete(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(rentService.complete(id));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<RentResponse> rate(@PathVariable("id") UUID id, @Valid @RequestBody RateRentRequest request) {
        return ResponseEntity.ok(rentService.rate(id, request.rating()));
    }
}
