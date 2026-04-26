package com.rentra.dto.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.user.UserStatus;

public record MeResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate birthDate,
        UserStatus status,
        OffsetDateTime createdAt) {}
