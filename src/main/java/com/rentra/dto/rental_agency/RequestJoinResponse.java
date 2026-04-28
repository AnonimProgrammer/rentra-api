package com.rentra.dto.rental_agency;

import java.util.UUID;

import com.rentra.domain.user.UserStatus;

public record RequestJoinResponse(UUID requestedUserId, UUID agencyId, UserStatus status) {}
