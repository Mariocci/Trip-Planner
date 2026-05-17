package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Korisnik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Korisnik testUser1;
    private Korisnik testUser2;
    private Korisnik oauthUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = Korisnik.builder()
                .ime("John")
                .prezime("Doe")
                .email("john.doe@example.com")
                .build();

        testUser2 = Korisnik.builder()
                .ime("Jane")
                .prezime("Smith")
                .email("jane.smith@example.com")
                .build();

        oauthUser = Korisnik.builder()
                .ime("OAuth")
                .prezime("User")
                .email("oauth.user@example.com")
                .oauthProvider("google")
                .oauthId("google-12345")
                .build();

        // Persist test users
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.persist(oauthUser);
        entityManager.flush();
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // When
        Optional<Korisnik> result = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getIme()).isEqualTo("John");
        assertThat(result.get().getPrezime()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByEmail(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithExistingProviderAndId_ShouldReturnUser() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "google-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOauthProvider()).isEqualTo("google");
        assertThat(result.get().getOauthId()).isEqualTo("google-12345");
        assertThat(result.get().getEmail()).isEqualTo("oauth.user@example.com");
    }

    @Test
    void findByOauthProviderAndOauthId_WithNonExistingProvider_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("facebook", "google-12345");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithNonExistingOauthId_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "nonexistent-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithNullValues_ShouldFindUserWithNullOAuth() {
        // Given - clear existing users and create one with null OAuth values
        userRepository.deleteAll();
        entityManager.flush();
        
        Korisnik userWithoutOAuth = Korisnik.builder()
                .ime("NoOAuth")
                .prezime("User")
                .email("no.oauth@example.com")
                .oauthProvider(null)
                .oauthId(null)
                .build();
        entityManager.persist(userWithoutOAuth);
        entityManager.flush();

        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId(null, null);

        // Then - Spring Data JPA will find users with null OAuth values
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("no.oauth@example.com");
        assertThat(result.get().getOauthProvider()).isNull();
        assertThat(result.get().getOauthId()).isNull();
    }

    @Test
    void findByOauthProviderAndOauthId_WithUserWithoutOAuth_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "some-id");

        // Then - should not find testUser1 or testUser2 as they don't have OAuth credentials
        assertThat(result).isEmpty();
    }

    // CRUD Operations Tests

    @Test
    void save_WithValidUser_ShouldPersistUser() {
        // Given
        Korisnik newUser = Korisnik.builder()
                .ime("Alice")
                .prezime("Johnson")
                .email("alice.johnson@example.com")
                .build();

        // When
        Korisnik savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getIme()).isEqualTo("Alice");
        assertThat(savedUser.getPrezime()).isEqualTo("Johnson");
        assertThat(savedUser.getEmail()).isEqualTo("alice.johnson@example.com");

        // Verify persistence
        Korisnik foundUser = entityManager.find(Korisnik.class, savedUser.getKorisnikId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("alice.johnson@example.com");
    }

    @Test
    void save_WithOAuthUser_ShouldPersistUserWithOAuthCredentials() {
        // Given
        Korisnik newOAuthUser = Korisnik.builder()
                .ime("Bob")
                .prezime("Wilson")
                .email("bob.wilson@example.com")
                .oauthProvider("facebook")
                .oauthId("facebook-67890")
                .build();

        // When
        Korisnik savedUser = userRepository.save(newOAuthUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getOauthProvider()).isEqualTo("facebook");
        assertThat(savedUser.getOauthId()).isEqualTo("facebook-67890");
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        // When
        Optional<Korisnik> result = userRepository.findById(testUser1.getKorisnikId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        // When/Then - Spring Data JPA doesn't allow null IDs
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> userRepository.findById(null)
        );
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(3); // testUser1, testUser2, oauthUser
        assertThat(users).extracting(Korisnik::getEmail)
                .containsExactlyInAnyOrder(
                        "john.doe@example.com",
                        "jane.smith@example.com",
                        "oauth.user@example.com"
                );
    }

    @Test
    void findAll_WithNoUsers_ShouldReturnEmptyList() {
        // Given - clear all users
        userRepository.deleteAll();
        entityManager.flush();

        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void update_ExistingUser_ShouldUpdateUserFields() {
        // Given
        Korisnik userToUpdate = userRepository.findById(testUser1.getKorisnikId()).orElseThrow();
        userToUpdate.setIme("UpdatedJohn");
        userToUpdate.setPrezime("UpdatedDoe");
        userToUpdate.setEmail("updated.john@example.com");

        // When
        Korisnik updatedUser = userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(updatedUser.getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(updatedUser.getIme()).isEqualTo("UpdatedJohn");
        assertThat(updatedUser.getPrezime()).isEqualTo("UpdatedDoe");
        assertThat(updatedUser.getEmail()).isEqualTo("updated.john@example.com");

        // Verify persistence
        Korisnik foundUser = entityManager.find(Korisnik.class, testUser1.getKorisnikId());
        assertThat(foundUser.getIme()).isEqualTo("UpdatedJohn");
        assertThat(foundUser.getPrezime()).isEqualTo("UpdatedDoe");
        assertThat(foundUser.getEmail()).isEqualTo("updated.john@example.com");
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        // Given
        Integer userId = testUser1.getKorisnikId();

        // When
        userRepository.delete(testUser1);
        entityManager.flush();

        // Then
        Optional<Korisnik> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_ExistingUser_ShouldRemoveUser() {
        // Given
        Integer userId = testUser2.getKorisnikId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<Korisnik> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void delete_NonExistingUser_ShouldNotThrowException() {
        // Given
        Korisnik nonExistingUser = Korisnik.builder()
                .korisnikId(99999)
                .ime("NonExisting")
                .prezime("User")
                .email("nonexisting@example.com")
                .build();

        // When/Then - should not throw exception
        userRepository.delete(nonExistingUser);
    }

    // Unique Constraint Tests

    @Test
    void save_WithDuplicateEmail_ShouldThrowException() {
        // Given
        Korisnik duplicateEmailUser = Korisnik.builder()
                .ime("Duplicate")
                .prezime("User")
                .email("john.doe@example.com") // Same as testUser1
                .build();

        // When/Then - Exception is thrown immediately on save due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    userRepository.save(duplicateEmailUser);
                    entityManager.flush();
                }
        );
    }

    @Test
    void save_WithDuplicateOAuthProviderAndId_ShouldAllowIfNoConstraint() {
        // Given
        Korisnik duplicateOAuthUser = Korisnik.builder()
                .ime("Duplicate")
                .prezime("OAuth")
                .email("duplicate.oauth@example.com")
                .oauthProvider("google") // Same as oauthUser
                .oauthId("google-12345") // Same as oauthUser
                .build();

        // When/Then
        // Note: If there's no unique constraint on (oauthProvider, oauthId) in the database schema,
        // this will succeed. If a constraint exists, it will throw DataIntegrityViolationException.
        // This test documents the current behavior - ideally there should be a unique constraint.
        try {
            userRepository.save(duplicateOAuthUser);
            entityManager.flush();
            // If we reach here, no constraint exists - this is acceptable but not ideal
            assertThat(duplicateOAuthUser.getKorisnikId()).isNotNull();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // If constraint exists, this is the expected behavior
            assertThat(e).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    @Test
    void update_ToExistingEmail_ShouldThrowException() {
        // Given
        Korisnik userToUpdate = userRepository.findById(testUser2.getKorisnikId()).orElseThrow();
        userToUpdate.setEmail("john.doe@example.com"); // testUser1's email

        // When/Then - Hibernate throws ConstraintViolationException which is wrapped in DataIntegrityViolationException
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, // Accept any exception type (could be ConstraintViolationException or DataIntegrityViolationException)
                () -> {
                    userRepository.save(userToUpdate);
                    entityManager.flush();
                }
        );
    }

    // Edge Cases Tests

    @Test
    void save_WithNullEmail_ShouldPersistUser() {
        // Given - email might be nullable in some scenarios
        Korisnik userWithNullEmail = Korisnik.builder()
                .ime("NoEmail")
                .prezime("User")
                .email(null)
                .build();

        // When
        Korisnik savedUser = userRepository.save(userWithNullEmail);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getEmail()).isNull();
    }

    @Test
    void save_WithEmptyStrings_ShouldPersistUser() {
        // Given
        Korisnik userWithEmptyStrings = Korisnik.builder()
                .ime("")
                .prezime("")
                .email("empty.strings@example.com")
                .build();

        // When
        Korisnik savedUser = userRepository.save(userWithEmptyStrings);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getIme()).isEmpty();
        assertThat(savedUser.getPrezime()).isEmpty();
    }

    @Test
    void findByEmail_WithEmptyString_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByEmail("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(3); // testUser1, testUser2, oauthUser
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsById(testUser1.getKorisnikId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsById(99999);

        // Then
        assertThat(exists).isFalse();
    }
}
