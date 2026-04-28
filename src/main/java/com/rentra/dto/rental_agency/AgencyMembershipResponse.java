package com.rentra.dto.rental_agency;

import java.util.UUID;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;

public record AgencyMembershipResponse(UUID userId, UUID agencyId, AgencyRole role, UserStatus status) {}
