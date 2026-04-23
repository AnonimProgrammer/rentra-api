package com.rentra.service.auth;

import com.rentra.domain.auth.AuthProviderEntity;
import com.rentra.domain.auth.ExternalIdentity;
import com.rentra.domain.auth.RoleEntity;
import com.rentra.domain.auth.RoleName;
import com.rentra.domain.auth.UserAuthEntity;
import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.auth.AuthContinueRequest;
import com.rentra.exception.auth.InvalidCredentialsException;
import com.rentra.exception.auth.ResourceNotFoundException;
import com.rentra.repository.auth.AuthProviderRepository;
import com.rentra.repository.auth.RoleRepository;
import com.rentra.repository.auth.UserAuthRepository;
import com.rentra.repository.user.UserRepository;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityMappingService {
  private final UserAuthRepository userAuthRepository;
  private final UserRepository userRepository;
  private final AuthProviderRepository authProviderRepository;
  private final RoleRepository roleRepository;

  public IdentityMappingService(
      UserAuthRepository userAuthRepository,
      UserRepository userRepository,
      AuthProviderRepository authProviderRepository,
      RoleRepository roleRepository) {
    this.userAuthRepository = userAuthRepository;
    this.userRepository = userRepository;
    this.authProviderRepository = authProviderRepository;
    this.roleRepository = roleRepository;
  }

  @Transactional
  public IdentityResolution resolve(
      ExternalIdentity identity, AuthContinueRequest.Profile profile, boolean canProvisionUser) {
    return userAuthRepository
        .findByProviderTypeAndProviderUserId(identity.provider(), identity.providerUserId())
        .map(userAuth -> new IdentityResolution(userAuth.getUser(), false))
        .orElseGet(() -> createNew(identity, profile, canProvisionUser));
  }

  private IdentityResolution createNew(
      ExternalIdentity identity, AuthContinueRequest.Profile profile, boolean canProvisionUser) {
    if (!canProvisionUser) {
      throw new InvalidCredentialsException("Account does not exist for this provider identity.");
    }

    if (profile == null) {
      throw new InvalidCredentialsException("Profile data is required for first-time sign-in.");
    }

    AuthProviderEntity provider =
        authProviderRepository
            .findByType(identity.provider())
            .orElseThrow(() -> new ResourceNotFoundException("Provider is not registered."));
    RoleEntity defaultRole =
        roleRepository
            .findByName(RoleName.CUSTOMER)
            .orElseThrow(() -> new ResourceNotFoundException("Default role not found."));

    UserEntity user = new UserEntity();
    user.setFirstName(required(profile.firstName(), "firstName"));
    user.setLastName(required(profile.lastName(), "lastName"));
    user.setEmail(required(firstNonBlank(profile.email(), identity.email()), "email"));
    user.setBirthDate(profile.birthDate());
    user.setStatus(UserStatus.ACTIVE);
    user.setRoles(Set.of(defaultRole));
    UserEntity savedUser = userRepository.save(user);

    UserAuthEntity userAuth = new UserAuthEntity();
    userAuth.setUser(savedUser);
    userAuth.setProvider(provider);
    userAuth.setProviderUserId(identity.providerUserId());
    userAuth.setEmail(firstNonBlank(identity.email(), profile.email()));
    userAuthRepository.save(userAuth);

    return new IdentityResolution(savedUser, true);
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new InvalidCredentialsException("Missing required profile field: " + field);
    }
    return value.trim();
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
}
