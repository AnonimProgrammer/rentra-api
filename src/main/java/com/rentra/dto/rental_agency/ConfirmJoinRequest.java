package com.rentra.dto.rental_agency;

import java.util.UUID;

import com.rentra.domain.rental_agency.AgencyRole;
import jakarta.validation.constraints.NotNull;

public record ConfirmJoinRequest(
        @NotNull(message = "Requested user id is required") UUID requestedUserId,
        @NotNull(message = "Role to assign is required") AgencyRole roleToAssign) {}
