package com.rentra.service.security.auth.provider;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.domain.auth.UserAuthEntity;
import com.rentra.exception.InvalidCredentialsException;
import com.rentra.repository.auth.UserAuthRepository;
import com.rentra.validation.Credentials;

@Component
public class UsernamePasswordAuthProvider implements AuthProvider {
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public UsernamePasswordAuthProvider(UserAuthRepository userAuthRepository, PasswordEncoder passwordEncoder) {
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.PASSWORD;
    }

    @Override
    public ExternalIdentity authenticate(Map<String, Object> credentials) {
        String identifier = Credentials.requiredNormalized(credentials, "username", "email");
        String password = Credentials.requiredRaw(credentials, "password");

        UserAuthEntity authEntry = userAuthRepository
                .findByProviderTypeAndProviderUserId(AuthProviderType.PASSWORD, identifier)
                .or(() -> userAuthRepository.findByProviderTypeAndEmail(AuthProviderType.PASSWORD, identifier))
                .orElse(null);

        if (authEntry == null) {
            return new ExternalIdentity(AuthProviderType.PASSWORD, identifier, identifier);
        }

        if (authEntry.getPasswordHash() == null || !passwordEncoder.matches(password, authEntry.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        String providerUserId = authEntry.getProviderUserId() == null ? identifier : authEntry.getProviderUserId();
        return new ExternalIdentity(AuthProviderType.PASSWORD, providerUserId, authEntry.getEmail());
    }
}
