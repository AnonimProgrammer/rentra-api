package com.rentra.dto.auth;

import java.time.LocalDate;
import java.util.Map;

import com.rentra.domain.auth.AuthProviderType;
import jakarta.validation.constraints.NotNull;

public record AuthContinueRequest(
        @NotNull(message = "Provider is required") AuthProviderType provider,
        @NotNull(message = "Credentials are required") Map<String, Object> credentials,
        Profile profile) {
    public record Profile(String firstName, String lastName, String email, LocalDate birthDate) {}
}
