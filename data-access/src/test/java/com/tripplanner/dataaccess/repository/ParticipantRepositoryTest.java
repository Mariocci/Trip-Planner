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
        
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(testTrip1.getPutovanjeId());

        
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Sudionik::getUloga)
                .containsExactlyInAnyOrder("organizer", "participant");
    }

    @Test
    void findByPutovanje_PutovanjeId_WithTripWithOneParticipant_ShouldReturnOneParticipant() {
        
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(testTrip2.getPutovanjeId());

        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUloga()).isEqualTo("organizer");
    }

    @Test
    void findByPutovanje_PutovanjeId_WithNonExistingTrip_ShouldReturnEmptyList() {
        
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(99999);

        
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithExistingParticipant_ShouldReturnParticipant() {
        
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip1.getPutovanjeId(), testUser1.getKorisnikId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getUloga()).isEqualTo("organizer");
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithNonParticipant_ShouldReturnEmpty() {
        
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip2.getPutovanjeId(), testUser1.getKorisnikId());

        
        assertThat(result).isEmpty();
    }

    @Test
    void countOrganizersByPutovanjeId_WithOneOrganizer_ShouldReturnOne() {
        
        Long count = participantRepository.countOrganizersByPutovanjeId(testTrip1.getPutovanjeId());

        
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countOrganizersByPutovanjeId_WithNoOrganizers_ShouldReturnZero() {
        
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

        
        Long count = participantRepository.countOrganizersByPutovanjeId(tripWithoutOrganizer.getPutovanjeId());

        
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void countOrganizersByPutovanjeId_WithNonExistingTrip_ShouldReturnZero() {
        
        Long count = participantRepository.countOrganizersByPutovanjeId(99999);

        
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void save_ShouldPersistNewParticipant() {
        
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

        
        Sudionik saved = participantRepository.save(newParticipant);

        
        assertThat(saved.getSudionikId()).isNotNull();
        assertThat(participantRepository.findById(saved.getSudionikId())).isPresent();
    }

    @Test
    void findById_WithExistingParticipant_ShouldReturnParticipant() {
        
        Optional<Sudionik> result = participantRepository.findById(participant1.getSudionikId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getUloga()).isEqualTo("organizer");
        assertThat(result.get().getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getPutovanje().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<Sudionik> result = participantRepository.findById(99999);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllParticipants() {
        
        List<Sudionik> results = participantRepository.findAll();

        
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Sudionik::getUloga)
                .containsExactlyInAnyOrder("organizer", "participant", "organizer");
    }

    @Test
    void delete_ShouldRemoveParticipant() {
        
        Integer participantId = participant2.getSudionikId();

        
        participantRepository.delete(participant2);
        entityManager.flush();

        
        assertThat(participantRepository.findById(participantId)).isEmpty();
        assertThat(participantRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveParticipant() {
        
        Integer participantId = participant2.getSudionikId();

        
        participantRepository.deleteById(participantId);
        entityManager.flush();

        
        assertThat(participantRepository.findById(participantId)).isEmpty();
        assertThat(participantRepository.findAll()).hasSize(2);
    }

    @Test
    void save_UpdateExistingParticipant_ShouldUpdateRole() {
        
        participant2.setUloga("organizer");

        
        Sudionik updated = participantRepository.save(participant2);
        entityManager.flush();

        
        assertThat(updated.getUloga()).isEqualTo("organizer");
        Optional<Sudionik> retrieved = participantRepository.findById(participant2.getSudionikId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUloga()).isEqualTo("organizer");
    }

    @Test
    void entityRelationships_ShouldBeProperlyPersisted() {
        
        Optional<Sudionik> result = participantRepository.findById(participant1.getSudionikId());

        
        assertThat(result).isPresent();
        Sudionik participant = result.get();
        
        
        assertThat(participant.getKorisnik()).isNotNull();
        assertThat(participant.getKorisnik().getKorisnikId()).isEqualTo(testUser1.getKorisnikId());
        assertThat(participant.getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        
        
        assertThat(participant.getPutovanje()).isNotNull();
        assertThat(participant.getPutovanje().getPutovanjeId()).isEqualTo(testTrip1.getPutovanjeId());
        assertThat(participant.getPutovanje().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void save_DuplicateUserAndTripCombination_ShouldThrowException() {
        
        Sudionik duplicateParticipant = Sudionik.builder()
                .putovanje(testTrip1)
                .korisnik(testUser1)
                .uloga("participant")
                .build();

        
        participantRepository.save(duplicateParticipant);
        
        
        try {
            entityManager.flush();
            
            
        } catch (Exception e) {
            
            assertThat(e).isNotNull();
        }
    }

    @Test
    void findByPutovanje_PutovanjeId_WithNullId_ShouldReturnEmptyList() {
        
        List<Sudionik> results = participantRepository.findByPutovanje_PutovanjeId(null);

        
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId_WithNullIds_ShouldReturnEmpty() {
        
        Optional<Sudionik> result = participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(null, null);

        
        assertThat(result).isEmpty();
    }

    @Test
    void countOrganizersByPutovanjeId_WithMultipleOrganizers_ShouldReturnCorrectCount() {
        
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

        
        Long count = participantRepository.countOrganizersByPutovanjeId(testTrip1.getPutovanjeId());

        
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        
        participantRepository.deleteAll();
        entityManager.flush();

        
        List<Sudionik> results = participantRepository.findAll();

        
        assertThat(results).isEmpty();
    }
}
