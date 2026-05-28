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

    

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        
        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));

        
        UserResponseDTO result = userService.getUserById(testUser.getKorisnikId());

        
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
        
        Integer nonExistentId = 999;
        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with ID: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
    }

    

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        
        UserResponseDTO result = userService.getUserByEmail(testUser.getEmail());

        
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(testUser.getKorisnikId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getIme()).isEqualTo(testUser.getIme());
        assertThat(result.getPrezime()).isEqualTo(testUser.getPrezime());

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void getUserByEmail_WithNonExistentEmail_ShouldThrowException() {
        
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: " + nonExistentEmail);

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getUserByEmail_WithInvalidEmailFormat_ShouldAttemptLookup() {
        
        String invalidEmail = "not-an-email";
        when(userRepository.findByEmail(invalidEmail))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> userService.getUserByEmail(invalidEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: " + invalidEmail);

        verify(userRepository).findByEmail(invalidEmail);
    }

    

    @Test
    void updateUserProfile_WithValidData_ShouldUpdateUser() {
        
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .prezime("Smith")
                .build();

        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Korisnik.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo("Jane");
        assertThat(result.getPrezime()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail()); 

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserProfile_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .build();

        when(userRepository.findById(testUser.getKorisnikId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Korisnik.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo("Jane");
        assertThat(result.getPrezime()).isEqualTo(testUser.getPrezime()); 

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserProfile_WithNonExistentUser_ShouldThrowException() {
        
        Integer nonExistentId = 999;
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .ime("Jane")
                .prezime("Smith")
                .build();

        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> userService.updateUserProfile(nonExistentId, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with ID: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(Korisnik.class));
    }

    @Test
    void updateUserProfile_WithNullFields_ShouldNotUpdateFields() {
        
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

        
        UserResponseDTO result = userService.updateUserProfile(testUser.getKorisnikId(), updateDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getIme()).isEqualTo(originalFirstName); 
        assertThat(result.getPrezime()).isEqualTo(originalLastName); 

        verify(userRepository).findById(testUser.getKorisnikId());
        verify(userRepository).save(testUser);
    }

    

    @Test
    void isEmailUnique_WithNewEmail_ShouldReturnTrue() {
        
        String newEmail = "new@example.com";
        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        
        boolean result = userService.isEmailUnique(newEmail, null);

        
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(newEmail);
    }

    @Test
    void isEmailUnique_WithExistingEmailAndSameUserId_ShouldReturnTrue() {
        
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        
        boolean result = userService.isEmailUnique(testUser.getEmail(), testUser.getKorisnikId());

        
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void isEmailUnique_WithExistingEmailAndDifferentUserId_ShouldReturnFalse() {
        
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        
        boolean result = userService.isEmailUnique(testUser.getEmail(), 999);

        
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void isEmailUnique_WithExistingEmailAndNullExcludeId_ShouldReturnFalse() {
        
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        
        boolean result = userService.isEmailUnique(testUser.getEmail(), null);

        
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    

    @Test
    void findOrCreateUserFromAuth0_WithExistingUser_ShouldReturnExistingUser() {
        
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                testUser.getEmail(),
                "John Doe",
                "auth0|123456",
                "https://example.com/picture.jpg"
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getKorisnikId()).isEqualTo(testUser.getKorisnikId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithNewUser_ShouldCreateUser() {
        
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

        
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                fullName,
                sub,
                "https://example.com/picture.jpg"
        );

        
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

        
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                singleName,
                sub,
                null
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("Madonna");
        assertThat(result.getPrezime()).isEqualTo("");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithNullName_ShouldHandleGracefully() {
        
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

        
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                null,
                sub,
                null
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("");
        assertThat(result.getPrezime()).isEqualTo("");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }

    @Test
    void findOrCreateUserFromAuth0_WithMultipleNameParts_ShouldSplitCorrectly() {
        
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

        
        UserResponseDTO result = userService.findOrCreateUserFromAuth0(
                newEmail,
                fullName,
                sub,
                null
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getIme()).isEqualTo("John");
        assertThat(result.getPrezime()).isEqualTo("Michael Smith");

        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(any(Korisnik.class));
    }
}
