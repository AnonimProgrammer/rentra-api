package com.rentra.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.dto.auth.AuthContinueResponse;
import com.rentra.service.auth.provider.AuthProvider;
import com.rentra.service.auth.provider.AuthProviderFactory;
import com.rentra.service.security.JwtTokenService;

@Service
public class AuthService {
    private final AuthProviderFactory authProviderFactory;
    private final IdentityMappingService identityMappingService;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            AuthProviderFactory authProviderFactory,
            IdentityMappingService identityMappingService,
            JwtTokenService jwtTokenService) {
        this.authProviderFactory = authProviderFactory;
        this.identityMappingService = identityMappingService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthContinueResponse continueAuthentication(AuthContinueRequest request) {
        AuthProvider provider = authProviderFactory.get(request.provider());
        ExternalIdentity identity = provider.authenticate(request.credentials());

        boolean canProvisionUser = request.provider() != AuthProviderType.PASSWORD;
        IdentityResolution resolution = identityMappingService.resolve(identity, request.profile(), canProvisionUser);

        String accessToken = jwtTokenService.issueAccessToken(resolution.user());
        String refreshToken = jwtTokenService.issueRefreshToken(resolution.user());
        return new AuthContinueResponse(accessToken, refreshToken, resolution.newUser());
    }
}
