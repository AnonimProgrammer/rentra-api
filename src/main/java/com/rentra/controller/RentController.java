package com.rentra.controller;

import java.util.List;
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
        return ResponseEntity.ok(rentService.complete(id));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<RentResponse> rate(@PathVariable("id") UUID id, @Valid @RequestBody RateRentRequest request) {
        return ResponseEntity.ok(rentService.rate(id, request.rating()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<RentResponse>> getActiveRents() {
        List<RentResponse> response = rentService.getActive();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/history")
    public ResponseEntity<PageResponse<RentResponse>> getMyCompletedRents(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) RentStatus status) {
        return ResponseEntity.ok(rentService.getMyRents(authService.getCurrentUserId(), cursor, limit, status));
    }
}
