package com.rentra.domain.rental_agency;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rentra.domain.user.UserStatus;

public interface MyAgencyMembershipProjection {
    UUID getAgencyId();

    String getAgencyName();

    AgencyRole getRole();

    UserStatus getStatus();

    OffsetDateTime getJoinedAt();
}
