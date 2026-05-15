package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.AuthService;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.entity.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

/**
 * Implementation of {@link AuthService} for authentication and authorization.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final Key jwtSecretKey;
    private final long jwtExpirationMs;

    public AuthServiceImpl(
            UserRepository userRepository,
            @Value("${jwt.secret:YOUR_JWT_SECRET_KEY_CHANGE_THIS_IN_PRODUCTION}") String jwtSecret,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public String initiateOAuthFlow(String provider) {
        validateProvider(provider);
        
        // In a real implementation, this would construct the OAuth authorization URL
        // For now, return a placeholder
        return "https://" + provider + ".com/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI";
    }

    @Override
    public Korisnik handleOAuthCallback(String code, String provider) {
        validateProvider(provider);
        
        // In a real implementation, this would:
        // 1. Exchange code for access token
        // 2. Fetch user info from OAuth provider
        // 3. Create or update user
        
        // For now, throw an exception indicating this needs OAuth configuration
        throw new UnsupportedOperationException(
            "OAuth callback handling requires OAuth provider configuration. " +
            "Please configure OAuth client credentials in application.properties"
        );
    }

    @Override
    public Korisnik createOrUpdateUser(String email, String firstName, String lastName, 
                                      String provider, String oauthId) {
        validateProvider(provider);
        
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (oauthId == null || oauthId.isBlank()) {
            throw new IllegalArgumentException("OAuth ID cannot be null or empty");
        }

        // Check if user exists by OAuth provider and ID
        Optional<Korisnik> existingUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        
        if (existingUser.isPresent()) {
            // Update existing user
            Korisnik user = existingUser.get();
            user.setIme(firstName);
            user.setPrezime(lastName);
            user.setEmail(email);
            return userRepository.save(user);
        } else {
            // Create new user
            Korisnik newUser = Korisnik.builder()
                    .ime(firstName)
                    .prezime(lastName)
                    .email(email)
                    .oauthProvider(provider)
                    .oauthId(oauthId)
                    .build();
            return userRepository.save(newUser);
        }
    }

    @Override
    public String generateSessionToken(Korisnik user) {
        if (user == null || user.getKorisnikId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getKorisnikId().toString())
                .claim("email", user.getEmail())
                .claim("userId", user.getKorisnikId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public Korisnik validateSessionToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Integer userId = claims.get("userId", Integer.class);
            
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for token"));
                    
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token: " + e.getMessage(), e);
        }
    }

    private void validateProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("OAuth provider cannot be null or empty");
        }
        
        if (!provider.equalsIgnoreCase("google") && !provider.equalsIgnoreCase("facebook")) {
            throw new IllegalArgumentException(
                "Unsupported OAuth provider: " + provider + ". Supported providers: google, facebook"
            );
        }
    }
}
