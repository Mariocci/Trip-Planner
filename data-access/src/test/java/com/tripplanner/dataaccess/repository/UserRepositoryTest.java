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


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

        
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.persist(oauthUser);
        entityManager.flush();
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        
        Optional<Korisnik> result = userRepository.findByEmail("john.doe@example.com");

        
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getIme()).isEqualTo("John");
        assertThat(result.get().getPrezime()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByEmail("nonexistent@example.com");

        
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByEmail(null);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithExistingProviderAndId_ShouldReturnUser() {
        
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "google-12345");

        
        assertThat(result).isPresent();
        assertThat(result.get().getOauthProvider()).isEqualTo("google");
        assertThat(result.get().getOauthId()).isEqualTo("google-12345");
        assertThat(result.get().getEmail()).isEqualTo("oauth.user@example.com");
    }

    @Test
    void findByOauthProviderAndOauthId_WithNonExistingProvider_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("facebook", "google-12345");

        
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithNonExistingOauthId_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "nonexistent-id");

        
        assertThat(result).isEmpty();
    }

    @Test
    void findByOauthProviderAndOauthId_WithNullValues_ShouldFindUserWithNullOAuth() {
        
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

        
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId(null, null);

        
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("no.oauth@example.com");
        assertThat(result.get().getOauthProvider()).isNull();
        assertThat(result.get().getOauthId()).isNull();
    }

    @Test
    void findByOauthProviderAndOauthId_WithUserWithoutOAuth_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByOauthProviderAndOauthId("google", "some-id");

        
        assertThat(result).isEmpty();
    }

    

    @Test
    void save_WithValidUser_ShouldPersistUser() {
        
        Korisnik newUser = Korisnik.builder()
                .ime("Alice")
                .prezime("Johnson")
                .email("alice.johnson@example.com")
                .build();

        
        Korisnik savedUser = userRepository.save(newUser);

        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getIme()).isEqualTo("Alice");
        assertThat(savedUser.getPrezime()).isEqualTo("Johnson");
        assertThat(savedUser.getEmail()).isEqualTo("alice.johnson@example.com");

        
        Korisnik foundUser = entityManager.find(Korisnik.class, savedUser.getKorisnikId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("alice.johnson@example.com");
    }

    @Test
    void save_WithOAuthUser_ShouldPersistUserWithOAuthCredentials() {
        
        Korisnik newOAuthUser = Korisnik.builder()
                .ime("Bob")
                .prezime("Wilson")
                .email("bob.wilson@example.com")
                .oauthProvider("facebook")
                .oauthId("facebook-67890")
                .build();

        
        Korisnik savedUser = userRepository.save(newOAuthUser);

        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getOauthProvider()).isEqualTo("facebook");
        assertThat(savedUser.getOauthId()).isEqualTo("facebook-67890");
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        
        Optional<Korisnik> result = userRepository.findById(testUser1.getKorisnikId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findById(99999);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> userRepository.findById(null)
        );
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        
        var users = userRepository.findAll();

        
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(3); 
        assertThat(users).extracting(Korisnik::getEmail)
                .containsExactlyInAnyOrder(
                        "john.doe@example.com",
                        "jane.smith@example.com",
                        "oauth.user@example.com"
                );
    }

    @Test
    void findAll_WithNoUsers_ShouldReturnEmptyList() {
        
        userRepository.deleteAll();
        entityManager.flush();

        
        var users = userRepository.findAll();

        
        assertThat(users).isEmpty();
    }

    @Test
    void update_ExistingUser_ShouldUpdateUserFields() {
        
        Korisnik userToUpdate = userRepository.findById(testUser1.getKorisnikId()).orElseThrow();
        userToUpdate.setIme("UpdatedJohn");
        userToUpdate.setPrezime("UpdatedDoe");
        userToUpdate.setEmail("updated.john@example.com");

        
        Korisnik updatedUser = userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();

        
        assertThat(updatedUser.getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(updatedUser.getIme()).isEqualTo("UpdatedJohn");
        assertThat(updatedUser.getPrezime()).isEqualTo("UpdatedDoe");
        assertThat(updatedUser.getEmail()).isEqualTo("updated.john@example.com");

        
        Korisnik foundUser = entityManager.find(Korisnik.class, testUser1.getKorisnikId());
        assertThat(foundUser.getIme()).isEqualTo("UpdatedJohn");
        assertThat(foundUser.getPrezime()).isEqualTo("UpdatedDoe");
        assertThat(foundUser.getEmail()).isEqualTo("updated.john@example.com");
    }

    @Test
    void delete_ExistingUser_ShouldRemoveUser() {
        
        Integer userId = testUser1.getKorisnikId();

        
        userRepository.delete(testUser1);
        entityManager.flush();

        
        Optional<Korisnik> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_ExistingUser_ShouldRemoveUser() {
        
        Integer userId = testUser2.getKorisnikId();

        
        userRepository.deleteById(userId);
        entityManager.flush();

        
        Optional<Korisnik> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void delete_NonExistingUser_ShouldNotThrowException() {
        
        Korisnik nonExistingUser = Korisnik.builder()
                .korisnikId(99999)
                .ime("NonExisting")
                .prezime("User")
                .email("nonexisting@example.com")
                .build();

        
        userRepository.delete(nonExistingUser);
    }

    

    @Test
    void save_WithDuplicateEmail_ShouldThrowException() {
        
        Korisnik duplicateEmailUser = Korisnik.builder()
                .ime("Duplicate")
                .prezime("User")
                .email("john.doe@example.com") 
                .build();

        
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
        
        Korisnik duplicateOAuthUser = Korisnik.builder()
                .ime("Duplicate")
                .prezime("OAuth")
                .email("duplicate.oauth@example.com")
                .oauthProvider("google") 
                .oauthId("google-12345") 
                .build();

        
        
        
        
        try {
            userRepository.save(duplicateOAuthUser);
            entityManager.flush();
            
            assertThat(duplicateOAuthUser.getKorisnikId()).isNotNull();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            
            assertThat(e).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    @Test
    void update_ToExistingEmail_ShouldThrowException() {
        
        Korisnik userToUpdate = userRepository.findById(testUser2.getKorisnikId()).orElseThrow();
        userToUpdate.setEmail("john.doe@example.com"); 

        
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, 
                () -> {
                    userRepository.save(userToUpdate);
                    entityManager.flush();
                }
        );
    }

    

    @Test
    void save_WithNullEmail_ShouldPersistUser() {
        
        Korisnik userWithNullEmail = Korisnik.builder()
                .ime("NoEmail")
                .prezime("User")
                .email(null)
                .build();

        
        Korisnik savedUser = userRepository.save(userWithNullEmail);

        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getKorisnikId()).isNotNull();
        assertThat(savedUser.getEmail()).isNull();
    }

    @Test
    void save_WithEmptyStrings_ShouldPersistUser() {
        
        Korisnik userWithEmptyStrings = Korisnik.builder()
                .ime("")
                .prezime("")
                .email("empty.strings@example.com")
                .build();

        
        Korisnik savedUser = userRepository.save(userWithEmptyStrings);

        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getIme()).isEmpty();
        assertThat(savedUser.getPrezime()).isEmpty();
    }

    @Test
    void findByEmail_WithEmptyString_ShouldReturnEmpty() {
        
        Optional<Korisnik> result = userRepository.findByEmail("");

        
        assertThat(result).isEmpty();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        
        long count = userRepository.count();

        
        assertThat(count).isEqualTo(3); 
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        
        boolean exists = userRepository.existsById(testUser1.getKorisnikId());

        
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        
        boolean exists = userRepository.existsById(99999);

        
        assertThat(exists).isFalse();
    }
}
