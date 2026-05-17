package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.UpdateUserDTO;
import com.tripplanner.domain.dto.UserResponseDTO;
import com.tripplanner.domain.entity.Korisnik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 * Tests user creation, updates, retrieval, and validation with mocked UserRepository.
 * 
 * Requirements: 2.1, 2.9, 2.13, 2.14, 2.15
 */
class UserServiceImplTest extends ServiceTestBase {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Korisnik testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    // ========== getUserById Tests ==========

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Given
        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.getUserById(testUser.getKorisnikId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(testUser.getKorisnikId());
        assertThat(result.getIme()).isEqualTo(testUser.getIme());
        assertThat(result.getPrezime()).isEqualTo(testUser.getPrezime());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getOauthProvider()).isEqualTo(testUser.getOauthProvider());

        verify(userRepository).findById(testUser.getKorisnikId());
    }

    @Test
    void getUserById_WithNonExistentId_ShouldThrowException() {
        // Given
        Integer nonExistentId = 999;
        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with ID: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
    }

    // ========== getUserByEmail Tests ==========

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.getUserByEmail(testUser.getEmail());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(testUser.getKorisnikId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getIme()).isEqualTo(testUser.getIme());
        assertThat(result.getPrezime()).isEqualTo(testUser.getPrezime());

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void getUserByEmail_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: " + nonExistentEmail);

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getUserByEmail_WithInvalidEmailFormat_ShouldAttemptLookup() {
        // Given - Testing that service doesn't validate email format, just looks it up
        String invalidEmail = "not-an-email";
        when(userRepository.findByEmail(invalidEmail))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail(invalidEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: " + invalidEmail);

        verify(userRepository).findByEmail(invalidEmail);
    }

    // ========== updateUserProfile Tests ==========

    @Test
    void updateUserProfile_WithValidData_ShouldUpdateUser() {
        // Given
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .prezime("Smith")
                .build();

        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Korisnik.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo("Jane");
        assertThat(result.getPrezime()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail()); // Email should not change

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserProfile_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given - Only update first name
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .build();

        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Korisnik.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo("Jane");
        assertThat(result.getPrezime()).isEqualTo(testUser.getPrezime()); // Should remain unchanged

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Given
        Integer nonExistentId = 999;
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .prezime("Smith")
                .build();

        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateUserProfile(nonExistentId, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with ID: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(Korisnik.class));
    }

    @Test
    void updateUserProfile_WithNullFields_ShouldNotUpdateFields() {
        // Given - DTO with null fields
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime(null)
                .prezime(null)
                .build();

        String originalFirstName = testUser.getIme();
        String originalLastName = testUser.getPrezime();

        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Korisnik.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo(originalFirstName); // Should remain unchanged
        assertThat(result.getPrezime()).isEqualTo(originalLastName); // Should remain unchanged

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    // ========== isEmailUnique Tests ==========

    @Test
    void isEmailUnique_WithNewEmail_ShouldReturnTrue() {
        // Given
        String newEmail = "new@example.com";
        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        // When
        boolean result = userService.isEmailUnique(newEmail, null);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(newEmail);
    }

    @Test
    void isEmailUnique_WithExistingEmailAndSameUserId_ShouldReturnTrue() {
        // Given - User updating their own email (no change)
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isEmailUnique(testUser.getEmail(), testUser.getKorisnikId());

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void isEmailUnique_WithExistingEmailAndDifferentUserId_ShouldReturnFalse() {
        // Given - Another user trying to use an existing email
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isEmailUnique(testUser.getEmail(), 999);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void isEmailUnique_WithExistingEmailAndNullExcludeId_ShouldReturnFalse() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isEmailUnique(testUser.getEmail(), null);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    // ========== findOrCreateUserFromAuth0 Tests ==========

    @Test
    void findOrCreateUserFromAuth0_WithExistingUser_ShouldReturnExistingUser() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                testUser.getEmail(),
                "John Doe",
                "auth0|123456",
                "https://example.com/picture.jpg"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(testUser.getKorisnikId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithNewUser_ShouldCreateUser() {
        // Given
        String newEmail = "newuser@example.com";
        String fullName = "John Smith";
        String sub = "auth0|123456";

        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        Korisnik savedUser = Korisnik.builder()
                .korisnikId(2)
                .email(newEmail)
                .ime("John")
                .prezime("Smith")
                .oauthProvider("auth0")
                .oauthId(sub)
                .build();

        when(userRepository.save(any(Korisnik.class)))
                .thenReturn(savedUser);

        // When
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                fullName,
                sub,
                "https://example.com/picture.jpg"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("John");
        assertThat(result.getPrezime()).isEqualTo("Smith");
        assertThat(result.getOauthProvider()).isEqualTo("auth0");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithSingleNamePart_ShouldHandleGracefully() {
        // Given
        String newEmail = "newuser@example.com";
        String singleName = "Madonna";
        String sub = "auth0|123456";

        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        Korisnik savedUser = Korisnik.builder()
                .korisnikId(2)
                .email(newEmail)
                .ime("Madonna")
                .prezime("")
                .oauthProvider("auth0")
                .oauthId(sub)
                .build();

        when(userRepository.save(any(Korisnik.class)))
                .thenReturn(savedUser);

        // When
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                singleName,
                sub,
                null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("Madonna");
        assertThat(result.getPrezime()).isEqualTo("");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithNullName_ShouldHandleGracefully() {
        // Given
        String newEmail = "newuser@example.com";
        String sub = "auth0|123456";

        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        Korisnik savedUser = Korisnik.builder()
                .korisnikId(2)
                .email(newEmail)
                .ime("")
                .prezime("")
                .oauthProvider("auth0")
                .oauthId(sub)
                .build();

        when(userRepository.save(any(Korisnik.class)))
                .thenReturn(savedUser);

        // When
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                null,
                sub,
                null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("");
        assertThat(result.getPrezime()).isEqualTo("");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithMultipleNameParts_ShouldSplitCorrectly() {
        // Given
        String newEmail = "newuser@example.com";
        String fullName = "John Michael Smith";
        String sub = "auth0|123456";

        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        Korisnik savedUser = Korisnik.builder()
                .korisnikId(2)
                .email(newEmail)
                .ime("John")
                .prezime("Michael Smith")
                .oauthProvider("auth0")
                .oauthId(sub)
                .build();

        when(userRepository.save(any(Korisnik.class)))
                .thenReturn(savedUser);

        // When
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                fullName,
                sub,
                null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("John");
        assertThat(result.getPrezime()).isEqualTo("Michael Smith");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }
}
