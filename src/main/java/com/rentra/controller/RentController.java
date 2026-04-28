package com.rentra.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentra.domain.rent.RentStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.rent.RateRentRequest;
import com.rentra.dto.rent.RentResponse;
import com.rentra.service.rent.RentService;
import com.rentra.service.security.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/rents")
@RequiredArgsConstructor
public class RentController {
    private final RentService rentService;
    private final AuthService authService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<RentResponse> complete(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(rentService.complete(id, authService.getCurrentUserId()));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<RentResponse> rate(@PathVariable("id") UUID id, @Valid @RequestBody RateRentRequest request) {
        return ResponseEntity.ok(rentService.rate(id, authService.getCurrentUserId(), request.rating()));
    }

    // Add admin endpoint to get all rents with pagination and filters

    @GetMapping("/me/history")
    public ResponseEntity<PageResponse<RentResponse>> getMyRents(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) RentStatus status) {
        return ResponseEntity.ok(rentService.getMyRents(authService.getCurrentUserId(), cursor, limit, status));
    }

    @GetMapping("/me/active")
    public ResponseEntity<RentResponse> getMyActiveRent() {
        return ResponseEntity.ok(rentService.getMyActive(authService.getCurrentUserId()));
    }
}
