package com.rentra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.google")
public record GoogleOAuth2Properties(String clientId) {
    public GoogleOAuth2Properties {
        clientId = clientId == null ? "" : clientId.trim();
    }
}
