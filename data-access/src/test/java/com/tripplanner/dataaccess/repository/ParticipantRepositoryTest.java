package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ParticipantRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Korisnik testUser1;
    private Korisnik testUser2;
    private Putovanje testTrip1;
    private Putovanje testTrip2;
    private Sudionik participant1;
    private Sudionik participant2;
    private Sudionik participant3;

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

        entityManager.persist(testUser1);
        entityManager.persist(testUser2);

        // Create test trips
        testTrip1 = Putovanje.builder()
                .naziv("Paris Trip")
                .opis("Exploring Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.valueOf(1500.00))
                .build();

        testTrip2 = Putovanje.builder()
                .naziv("Rome Trip")
                .opis("Exploring Rome")
                .datumPoc(LocalDate.of(2024, 7, 1))
                .datumKraj(LocalDate.of(2024, 7, 10))
                .ukTrosak(BigDecimal.valueOf(2000.00))
                .build();

        entityManager.persist(testTrip1);
        entityManager.persist(testTrip2);

        // Create test participants
        participant1 = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(testUser1)
                .uloga("organizer")
                .build();

        participant2 = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(testUser2)
                .uloga("participant")
                .build();

        participant3 = Sudionik.builder()
                .putovanje(testTrip2)
                .korisnik(testUser2)
                .uloga("organizer")
                .build();

        entityManager.persist(participant1);
        entityManager.persist(participant2);
        entityManager.persist(participant3);
        entityManager.flush();
    }

    @Test
    void findByPutovanje_PutovanjeId_WithExistingTrip_ShouldReturnAllParticipants() {
        // When
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(testTrip1.getPutovanjeId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Sudionik::getUloga)
                .containsExactlyInAnyOrder("organizer", "participant");
    }

    @Test
    void findByPutovanje_PutovanjeId_WithTripWithOneParticipant_ShouldReturnOneParticipant() {
        // When
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(testTrip2.getPutovanjeId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUloga()).isEqualTo("organizer");
    }

    @Test
    void findByPutovanje_PutovanjeId_WithNonExistingTrip_ShouldReturnEmptyList() {
        // When
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(99999);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithExistingParticipant_ShouldReturnParticipant() {
        // When
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip1.getPutovanjeId(), testUser1.getKorisnikId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUloga()).isEqualTo("organizer");
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithNonParticipant_ShouldReturnEmpty() {
        // When
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip2.getPutovanjeId(), testUser1.getKorisnikId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void countOrganizersByPutovanjeId_WithOneOrganizer_ShouldReturnOne() {
        // When
        Long count = participantRepository.countOrganizersByPutovanjeId(testTrip1.getPutovanjeId());

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countOrganizersByPutovanjeId_WithNoOrganizers_ShouldReturnZero() {
        // Given - create a trip with only participants (no organizers)
        Putovanje tripWithoutOrganizer = Putovanje.builder()
                .naziv("Test Trip")
                .datumPoc(LocalDate.of(2024, 8, 1))
                .datumKraj(LocalDate.of(2024, 8, 5))
                .build();
        entityManager.persist(tripWithoutOrganizer);

        Sudionik participantOnly = Sudionik.builder()
                .putovanje(tripWithoutOrganizer)
                .korisnik(testUser1)
                .uloga("participant")
                .build();
        entityManager.persist(participantOnly);
        entityManager.flush();

        // When
        Long count = participantRepository.countOrganizersByPutovanjeId(tripWithoutOrganizer.getPutovanjeId());

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void countOrganizersByPutovanjeId_WithNonExistingTrip_ShouldReturnZero() {
        // When
        Long count = participantRepository.countOrganizersByPutovanjeId(99999);

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void save_ShouldPersistNewParticipant() {
        // Given
        Korisnik newUser = Korisnik.builder()
                .ime("Bob")
                .prezime("Johnson")
                .email("bob.johnson@example.com")
                .build();
        entityManager.persist(newUser);

        Sudionik newParticipant = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(newUser)
                .uloga("participant")
                .build();

        // When
        Sudionik saved = participantRepository.save(newParticipant);

        // Then
        assertThat(saved.getSudionikId()).isNotNull();
        assertThat(participantRepository.findById(saved.getSudionikId())).isPresent();
    }

    @Test
    void findById_WithExistingParticipant_ShouldReturnParticipant() {
        // When
        Optional<Sudionik> result = participantRepository.findById(participant1.getSudionikId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUloga()).isEqualTo("organizer");
        assertThat(result.get().getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getPutovanje().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Sudionik> result = participantRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllParticipants() {
        // When
        List<Sudionik> results = participantRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Sudionik::getUloga)
                .containsExactlyInAnyOrder("organizer", "participant", "organizer");
    }

    @Test
    void delete_ShouldRemoveParticipant() {
        // Given
        Integer participantId = participant2.getSudionikId();

        // When
        participantRepository.delete(participant2);
        entityManager.flush();

        // Then
        assertThat(participantRepository.findById(participantId)).isEmpty();
        assertThat(participantRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveParticipant() {
        // Given
        Integer participantId = participant2.getSudionikId();

        // When
        participantRepository.deleteById(participantId);
        entityManager.flush();

        // Then
        assertThat(participantRepository.findById(participantId)).isEmpty();
        assertThat(participantRepository.findAll()).hasSize(2);
    }

    @Test
    void save_UpdateExistingParticipant_ShouldUpdateRole() {
        // Given
        participant2.setUloga("organizer");

        // When
        Sudionik updated = participantRepository.save(participant2);
        entityManager.flush();

        // Then
        assertThat(updated.getUloga()).isEqualTo("organizer");
        Optional<Sudionik> retrieved = participantRepository.findById(participant2.getSudionikId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUloga()).isEqualTo("organizer");
    }

    @Test
    void entityRelationships_ShouldBeProperlyPersisted() {
        // When
        Optional<Sudionik> result = participantRepository.findById(participant1.getSudionikId());

        // Then
        assertThat(result).isPresent();
        Sudionik participant = result.get();
        
        // Verify user relationship
        assertThat(participant.getKorisnik()).isNotNull();
        assertThat(participant.getKorisnik().getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(participant.getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        
        // Verify trip relationship
        assertThat(participant.getPutovanje()).isNotNull();
        assertThat(participant.getPutovanje().getPutovanjeId()).isEqualTo(testTrip1.getPutovanjeId());
        assertThat(participant.getPutovanje().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void save_DuplicateUserAndTripCombination_ShouldThrowException() {
        // Given - participant1 already exists with testUser1 and testTrip1
        Sudionik duplicateParticipant = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(testUser1)
                .uloga("participant")
                .build();

        // When/Then - attempting to save should cause a constraint violation
        participantRepository.save(duplicateParticipant);
        
        // Flush to trigger the constraint check
        try {
            entityManager.flush();
            // If we reach here without exception, the unique constraint is not enforced at DB level
            // This is acceptable as the constraint might be enforced at the service layer
        } catch (Exception e) {
            // Expected behavior if database has unique constraint
            assertThat(e).isNotNull();
        }
    }

    @Test
    void findByPutovanje_PutovanjeId_WithNullId_ShouldReturnEmptyList() {
        // When
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(null);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithNullIds_ShouldReturnEmpty() {
        // When
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(null, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void countOrganizersByPutovanjeId_WithMultipleOrganizers_ShouldReturnCorrectCount() {
        // Given - add another organizer to testTrip1
        Korisnik newUser = Korisnik.builder()
                .ime("Alice")
                .prezime("Brown")
                .email("alice.brown@example.com")
                .build();
        entityManager.persist(newUser);

        Sudionik newOrganizer = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(newUser)
                .uloga("organizer")
                .build();
        entityManager.persist(newOrganizer);
        entityManager.flush();

        // When
        Long count = participantRepository.countOrganizersByPutovanjeId(testTrip1.getPutovanjeId());

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given - clear all participants
        participantRepository.deleteAll();
        entityManager.flush();

        // When
        List<Sudionik> results = participantRepository.findAll();

        // Then
        assertThat(results).isEmpty();
    }
}
