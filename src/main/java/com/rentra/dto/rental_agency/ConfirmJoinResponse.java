package com.rentra.dto.rental_agency;

import java.util.UUID;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;

public record ConfirmJoinResponse(UUID requestedUserId, UUID agencyId, AgencyRole role, UserStatus status) {}
