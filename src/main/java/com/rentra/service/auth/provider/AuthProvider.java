package com.rentra.service.auth.provider;

import java.util.Map;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;

public interface AuthProvider {
    AuthProviderType getType();

    ExternalIdentity authenticate(Map<String, Object> credentials);
}
