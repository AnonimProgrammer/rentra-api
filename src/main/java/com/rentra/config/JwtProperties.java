package com.rentra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(String secret, long accessTtlSeconds, long refreshTtlSeconds) {}
