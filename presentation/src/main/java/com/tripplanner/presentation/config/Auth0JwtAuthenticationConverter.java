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


public class Auth0JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, extractUsername(jwt));
    }

    
    private String extractUsername(Jwt jwt) {
        if (jwt.hasClaim("email")) {
            return jwt.getClaimAsString("email");
        }
        return jwt.getSubject();
    }

    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        
        if (jwt.hasClaim("permissions")) {
            Collection<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                return permissions.stream()
                    .map(permission -> new SimpleGrantedAuthority("SCOPE_" + permission))
                    .collect(Collectors.toList());
            }
        }

        
        String namespace = "https://tripplanner.com/";
        if (jwt.hasClaim(namespace + "roles")) {
            Collection<String> roles = jwt.getClaimAsStringList(namespace + "roles");
            if (roles != null) {
                return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            }
        }

        
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
