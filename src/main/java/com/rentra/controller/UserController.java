package com.rentra.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentra.dto.user.MeResponse;
import com.rentra.dto.user.UserResponse;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.user.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(userService.getMeById(authService.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // Add admin endpoint to get all users with pagination and filters

    // Add admin endpoint to patch user: status, role
}
