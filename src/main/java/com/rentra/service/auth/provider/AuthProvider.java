package com.rentra.service.auth.provider;

import com.rentra.domain.auth.AuthProviderType;
import com.rentra.domain.auth.ExternalIdentity;
import java.util.Map;

public interface AuthProvider {
  AuthProviderType getType();

  ExternalIdentity authenticate(Map<String, Object> credentials);
}
