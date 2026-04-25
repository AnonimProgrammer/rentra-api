package com.rentra.service.security.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTH_ERROR_MESSAGE_ATTRIBUTE = "authErrorMessage";
    private static final String MISSING_AUTH_DETAILS_MESSAGE = "No authentication details were provided.";
    private static final String INVALID_OR_EXPIRED_TOKEN_MESSAGE = "JWT token is expired or invalid.";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            request.setAttribute(AUTH_ERROR_MESSAGE_ATTRIBUTE, MISSING_AUTH_DETAILS_MESSAGE);
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (!jwtTokenService.isValidToken(token)) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_MESSAGE_ATTRIBUTE, INVALID_OR_EXPIRED_TOKEN_MESSAGE);
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtTokenService.parseAccessToken(token);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwtTokenService.extractUserId(claims), null, extractAuthorities(claims));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object value = claims.get("roles");
        if (!(value instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
