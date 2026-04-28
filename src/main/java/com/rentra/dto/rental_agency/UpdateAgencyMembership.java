package com.rentra.dto.rental_agency;

import java.util.UUID;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAgencyMembership(
        @NotNull(message = "User id is required") UUID userId, UserStatus status, AgencyRole role) {}
