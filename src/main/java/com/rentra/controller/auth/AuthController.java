package com.rentra.controller.auth;

import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.dto.auth.AuthContinueResponse;
import com.rentra.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/continue")
  public ResponseEntity<AuthContinueResponse> continueAuth(
      @Valid @RequestBody AuthContinueRequest request) {
    return ResponseEntity.ok(authService.continueAuthentication(request));
  }
}
