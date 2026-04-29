package com.rentra.dto.rental_agency;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;

public record MyAgencyMembershipResponse(
        UUID agencyId, String agencyName, AgencyRole role, UserStatus status, OffsetDateTime joinedAt) {}
