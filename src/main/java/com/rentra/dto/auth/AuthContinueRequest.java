package com.rentra.dto.auth;

import com.rentra.domain.auth.AuthProviderType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

public record AuthContinueRequest(
    @NotNull AuthProviderType provider,
    @NotNull Map<String, Object> credentials,
    Profile profile) {
  public record Profile(String firstName, String lastName, String email, LocalDate birthDate) {}
}
