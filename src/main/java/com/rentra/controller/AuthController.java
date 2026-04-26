package com.rentra.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.dto.auth.AuthContinueResponse;
import com.rentra.service.security.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/continue")
    public ResponseEntity<AuthContinueResponse> continueAuth(@Valid @RequestBody AuthContinueRequest request) {
        return ResponseEntity.ok(authService.continueAuthentication(request));
    }
}
