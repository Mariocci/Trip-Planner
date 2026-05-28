package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.AuthService;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.entity.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;


@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SecretKey jwtSecretKey;
    private final long jwtExpirationMs;

    public AuthServiceImpl(
            UserRepository userRepository,
            @Value("${jwt.secret:YOUR_JWT_SECRET_KEY_CHANGE_THIS_IN_PRODUCTION}") String jwtSecret,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public String initiateOAuthFlow(String provider) {
        validateProvider(provider);
        
        
        
        return "https://" + provider + ".com/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI";
    }

    @Override
    public Korisnik handleOAuthCallback(String code, String provider) {
        validateProvider(provider);
        
        
        
        
        
        
        
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

        
        Optional<Korisnik> existingUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        
        if (existingUser.isPresent()) {
            
            Korisnik user = existingUser.get();
            user.setIme(firstName);
            user.setPrezime(lastName);
            user.setEmail(email);
            return userRepository.save(user);
        } else {
            
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
                .subject(user.getKorisnikId().toString())
                .claim("email", user.getEmail())
                .claim("userId", user.getKorisnikId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    @Override
    public Korisnik validateSessionToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

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
