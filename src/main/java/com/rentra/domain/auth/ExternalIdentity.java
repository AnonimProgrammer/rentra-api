package com.rentra.domain.auth;

public record ExternalIdentity(AuthProviderType provider, String providerUserId, String email) {}
