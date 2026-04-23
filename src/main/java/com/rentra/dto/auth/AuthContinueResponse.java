package com.rentra.dto.auth;

public record AuthContinueResponse(String accessToken, String refreshToken, boolean newUser) {}
