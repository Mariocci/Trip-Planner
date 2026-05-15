package com.tripplanner.business.service.impl;

import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.entity.Korisnik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                "test-secret-key-for-jwt-token-generation-minimum-256-bits",
                86400000L // 24 hours
        );
    }

    @Test
    void initiateOAuthFlow_WithValidProvider_ShouldReturnAuthorizationUrl() {
        // When
        String googleUrl = authService.initiateOAuthFlow("google");
        String facebookUrl = authService.initiateOAuthFlow("facebook");

        // Then
        assertThat(googleUrl).contains("google.com/oauth/authorize");
        assertThat(facebookUrl).contains("facebook.com/oauth/authorize");
    }

    @Test
    void initiateOAuthFlow_WithInvalidProvider_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.initiateOAuthFlow("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported OAuth provider");
    }

    @Test
    void initiateOAuthFlow_WithNullProvider_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.initiateOAuthFlow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OAuth provider cannot be null or empty");
    }

    @Test
    void createOrUpdateUser_WithNewUser_ShouldCreateUser() {
        // Given
        when(userRepository.findByOauthProviderAndOauthId("google", "google-123"))
                .thenReturn(Optional.empty());
        
        Korisnik savedUser = Korisnik.builder()
                .korisnikId(1)
                .ime("John")
                .prezime("Doe")
                .email("john@example.com")
                .oauthProvider("google")
                .oauthId("google-123")
                .build();
        
        when(userRepository.save(any(Korisnik.class))).thenReturn(savedUser);

        // When
        Korisnik result = authService.createOrUpdateUser(
                "john@example.com", "John", "Doe", "google", "google-123"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getIme()).isEqualTo("John");
        assertThat(result.getPrezime()).isEqualTo("Doe");
        assertThat(result.getOauthProvider()).isEqualTo("google");
        assertThat(result.getOauthId()).isEqualTo("google-123");
        
        verify(userRepository).findByOauthProviderAndOauthId("google", "google-123");
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void createOrUpdateUser_WithExistingUser_ShouldUpdateUser() {
        // Given
        Korisnik existingUser = Korisnik.builder()
                .korisnikId(1)
                .ime("OldName")
                .prezime("OldSurname")
                .email("old@example.com")
                .oauthProvider("google")
                .oauthId("google-123")
                .build();
        
        when(userRepository.findByOauthProviderAndOauthId("google", "google-123"))
                .thenReturn(Optional.of(existingUser));
        
        when(userRepository.save(any(Korisnik.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Korisnik result = authService.createOrUpdateUser(
                "new@example.com", "NewName", "NewSurname", "google", "google-123"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getIme()).isEqualTo("NewName");
        assertThat(result.getPrezime()).isEqualTo("NewSurname");
        
        verify(userRepository).findByOauthProviderAndOauthId("google", "google-123");
        verify(userRepository).save(existingUser);
    }

    @Test
    void createOrUpdateUser_WithNullEmail_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.createOrUpdateUser(
                null, "John", "Doe", "google", "google-123"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null or empty");
    }

    @Test
    void createOrUpdateUser_WithNullOAuthId_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.createOrUpdateUser(
                "john@example.com", "John", "Doe", "google", null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OAuth ID cannot be null or empty");
    }

    @Test
    void createOrUpdateUser_WithInvalidProvider_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.createOrUpdateUser(
                "john@example.com", "John", "Doe", "invalid", "oauth-123"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported OAuth provider");
    }

    @Test
    void generateSessionToken_WithValidUser_ShouldReturnToken() {
        // Given
        Korisnik user = Korisnik.builder()
                .korisnikId(1)
                .email("john@example.com")
                .build();

        // When
        String token = authService.generateSessionToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void generateSessionToken_WithNullUser_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.generateSessionToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User and user ID cannot be null");
    }

    @Test
    void validateSessionToken_WithValidToken_ShouldReturnUser() {
        // Given
        Korisnik user = Korisnik.builder()
                .korisnikId(1)
                .email("john@example.com")
                .build();
        
        String token = authService.generateSessionToken(user);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // When
        Korisnik result = authService.validateSessionToken(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        
        verify(userRepository).findById(1);
    }

    @Test
    void validateSessionToken_WithInvalidToken_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.validateSessionToken("invalid.token.here"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    void validateSessionToken_WithNullToken_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> authService.validateSessionToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token cannot be null or empty");
    }

    @Test
    void validateSessionToken_WithUserNotFound_ShouldThrowException() {
        // Given
        Korisnik user = Korisnik.builder()
                .korisnikId(1)
                .email("john@example.com")
                .build();
        
        String token = authService.generateSessionToken(user);
        
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.validateSessionToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found for token");
    }
}
