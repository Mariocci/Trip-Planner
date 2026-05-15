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
    void findByOauthProviderAndOauthId_WithNullValues_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId(null, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithUserWithoutOAuth_ShouldReturnEmpty() {
        // When
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "some-id");

        // Then - should not find testUser1 or testUser2 as they don't have OAuth credentials
        assertThat(result).isEmpty();
    }
}
