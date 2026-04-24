package com.rentra.service.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.rentra.config.JwtProperties;
import com.rentra.domain.auth.RoleEntity;
import com.rentra.domain.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {
    private final SecretKey signingKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtl = Duration.ofSeconds(jwtProperties.accessTtlSeconds());
        this.refreshTtl = Duration.ofSeconds(jwtProperties.refreshTtlSeconds());
    }

    public String issueAccessToken(UserEntity user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .map(Enum::name)
                .toList();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .claim("roles", roles)
                .signWith(signingKey)
                .compact();
    }

    public String issueRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtl)))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }
}
