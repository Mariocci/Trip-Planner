package com.tripplanner.presentation.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Converts Auth0 JWT tokens to Spring Security Authentication objects.
 * Extracts user information and authorities from the JWT claims.
 */
public class Auth0JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, extractUsername(jwt));
    }

    /**
     * Extracts username from JWT token.
     * Tries 'email' claim first, falls back to 'sub' (subject).
     */
    private String extractUsername(Jwt jwt) {
        if (jwt.hasClaim("email")) {
            return jwt.getClaimAsString("email");
        }
        return jwt.getSubject();
    }

    /**
     * Extracts authorities/roles from JWT token.
     * Auth0 stores permissions in different claim formats depending on configuration.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Check for permissions in Auth0 format
        if (jwt.hasClaim("permissions")) {
            Collection<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                return permissions.stream()
                    .map(permission -> new SimpleGrantedAuthority("SCOPE_" + permission))
                    .collect(Collectors.toList());
            }
        }

        // Check for roles in custom namespace
        String namespace = "https://tripplanner.com/";
        if (jwt.hasClaim(namespace + "roles")) {
            Collection<String> roles = jwt.getClaimAsStringList(namespace + "roles");
            if (roles != null) {
                return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            }
        }

        // Default: grant basic user authority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
