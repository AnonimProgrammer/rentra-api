package com.rentra.service.security.auth.provider;

import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.exception.InvalidCredentialsException;
import com.rentra.service.security.jwt.GoogleJwtTokenService;
import com.rentra.validation.Credentials;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleAuthProvider implements AuthProvider {
    private final GoogleJwtTokenService googleJwtTokenService;

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.GOOGLE;
    }

    @Override
    public ExternalIdentity authenticate(Map<String, Object> credentials) {
        String idToken = Credentials.requiredRaw(credentials, "idToken", "identityToken", "token");
        Jwt jwt = googleJwtTokenService.decodeIdentityToken(idToken);

        String providerUserId = jwt.getSubject();
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new InvalidCredentialsException("Google identity token missing subject.");
        }

        String email = normalize(jwt.getClaimAsString("email"));
        if (!googleJwtTokenService.isEmailVerified(jwt)) {
            throw new InvalidCredentialsException("Google account email is not verified.");
        }

        return new ExternalIdentity(AuthProviderType.GOOGLE, providerUserId, email);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
