package com.rentra.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {}
