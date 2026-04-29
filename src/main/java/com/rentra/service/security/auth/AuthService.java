package com.rentra.service.security.auth;

import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.dto.auth.AuthContinueResponse;
import com.rentra.dto.auth.AuthRefreshResponse;
import com.rentra.exception.InvalidCredentialsException;
import com.rentra.service.security.auth.provider.AuthProvider;
import com.rentra.service.security.auth.provider.AuthProviderFactory;
import com.rentra.service.security.jwt.JwtTokenService;
import com.rentra.service.user.UserService;
import com.rentra.validation.Credentials;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthProviderFactory authProviderFactory;
    private final IdentityMappingService identityMappingService;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Transactional
    public AuthContinueResponse continueAuthentication(AuthContinueRequest request) {
        AuthProvider provider = authProviderFactory.get(request.provider());
        ExternalIdentity identity = provider.authenticate(request.credentials());

        String password = Credentials.optionalRaw(request.credentials(), "password");
        IdentityResolution resolution = identityMappingService.resolve(identity, request.profile(), password);

        String accessToken = jwtTokenService.issueAccessToken(resolution.user());
        String refreshToken = jwtTokenService.issueRefreshToken(resolution.user());
        return new AuthContinueResponse(accessToken, refreshToken, resolution.newUser());
    }

    @Transactional(readOnly = true)
    public AuthRefreshResponse refresh(String refreshToken) {
        try {
            Claims claims = jwtTokenService.parseRefreshToken(refreshToken);
            UUID userId = jwtTokenService.extractUserId(claims);

            var user = userService.findOrThrow(userId);
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthorizationDeniedException("Access denied for inactive user.");
            }

            String newAccessToken = jwtTokenService.issueAccessToken(user);
            String newRefreshToken = jwtTokenService.issueRefreshToken(user);
            return new AuthRefreshResponse(newAccessToken, newRefreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidCredentialsException("Invalid refresh token.");
        }
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new InvalidCredentialsException("No authenticated user in security context.");
        }
        return userId;
    }
}
