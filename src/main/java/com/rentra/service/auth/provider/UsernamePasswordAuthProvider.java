package com.rentra.service.auth.provider;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.domain.auth.UserAuthEntity;
import com.rentra.exception.auth.InvalidCredentialsException;
import com.rentra.repository.auth.UserAuthRepository;

@Component
public class UsernamePasswordAuthProvider implements AuthProvider {
    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String PASSWORD_KEY = "password";

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
        String identifier = normalizedCredential(credentials, USERNAME_KEY, EMAIL_KEY);
        String password = rawCredential(credentials, PASSWORD_KEY);

        UserAuthEntity authEntry = userAuthRepository
                .findByProviderTypeAndProviderUserId(AuthProviderType.PASSWORD, identifier)
                .or(() -> userAuthRepository.findByProviderTypeAndEmail(AuthProviderType.PASSWORD, identifier))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (authEntry.getPasswordHash() == null || !passwordEncoder.matches(password, authEntry.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        String providerUserId = authEntry.getProviderUserId() == null ? identifier : authEntry.getProviderUserId();
        return new ExternalIdentity(AuthProviderType.PASSWORD, providerUserId, authEntry.getEmail());
    }

    private String normalizedCredential(Map<String, Object> credentials, String... keys) {
        for (String key : keys) {
            Object value = credentials.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text.trim().toLowerCase();
            }
        }
        throw new InvalidCredentialsException("Missing credential: " + String.join(" or ", keys));
    }

    private String rawCredential(Map<String, Object> credentials, String... keys) {
        for (String key : keys) {
            Object value = credentials.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        throw new InvalidCredentialsException("Missing credential: " + String.join(" or ", keys));
    }
}
