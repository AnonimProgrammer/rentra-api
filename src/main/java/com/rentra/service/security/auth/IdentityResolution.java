package com.rentra.service.security.auth;

import com.rentra.domain.user.UserEntity;

public record IdentityResolution(UserEntity user, boolean newUser) {}
