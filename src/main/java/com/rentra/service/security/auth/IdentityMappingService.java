package com.rentra.service.security.auth;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentra.domain.auth.AuthProviderEntity;
import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.domain.auth.RoleEntity;
import com.rentra.domain.auth.RoleName;
import com.rentra.domain.auth.UserAuthEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.exception.InvalidCredentialsException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.repository.auth.AuthProviderRepository;
import com.rentra.repository.auth.RoleRepository;
import com.rentra.repository.auth.UserAuthRepository;
import com.rentra.repository.user.UserRepository;
import com.rentra.validation.Preconditions;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityMappingService {
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public IdentityResolution resolve(
            ExternalIdentity identity, AuthContinueRequest.Profile profile, String rawPassword) {
        return userAuthRepository
                .findByProviderTypeAndProviderUserId(identity.provider(), identity.providerUserId())
                .map(userAuth -> new IdentityResolution(userAuth.getUser(), false))
                .orElseGet(() -> createNew(identity, profile, rawPassword));
    }

    private IdentityResolution createNew(
            ExternalIdentity identity, AuthContinueRequest.Profile profile, String rawPassword) {
        if (profile == null) {
            throw new InvalidCredentialsException("Profile data is required for first-time sign-in.");
        }

        AuthProviderEntity provider = authProviderRepository
                .findByType(identity.provider())
                .orElseThrow(() -> new ResourceNotFoundException("Provider is not registered."));
        RoleEntity defaultRole = roleRepository
                .findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found."));

        UserEntity user = new UserEntity();
        user.setFirstName(Preconditions.required(profile.firstName(), "Missing required field: firstName"));
        user.setLastName(Preconditions.required(profile.lastName(), "Missing required field: lastName"));
        user.setEmail(Preconditions.required(
                firstNonBlank(profile.email(), identity.email()), "Missing required field: email"));
        user.setBirthDate(profile.birthDate());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(defaultRole));
        UserEntity savedUser = userRepository.save(user);

        UserAuthEntity userAuth = new UserAuthEntity();
        userAuth.setUser(savedUser);
        userAuth.setProvider(provider);
        userAuth.setProviderUserId(identity.providerUserId());
        userAuth.setEmail(firstNonBlank(identity.email(), profile.email()));
        if (identity.provider() == AuthProviderType.PASSWORD) {
            userAuth.setPasswordHash(encodePassword(rawPassword));
        }
        userAuthRepository.save(userAuth);

        return new IdentityResolution(savedUser, true);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim().toLowerCase();
        }
        if (second != null && !second.isBlank()) {
            return second.trim().toLowerCase();
        }
        return null;
    }

    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException("Password is required for password registration.");
        }
        return passwordEncoder.encode(rawPassword);
    }
}
