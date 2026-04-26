package com.rentra.dto.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.rentra.domain.auth.RoleName;
import com.rentra.domain.user.UserStatus;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate birthDate,
        UserStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<RoleName> roles) {}
