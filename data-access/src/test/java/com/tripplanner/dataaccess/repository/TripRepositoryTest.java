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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TripRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TripRepositoryTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Korisnik testUser1;
    private Korisnik testUser2;
    private Putovanje trip1;
    private Putovanje trip2;
    private Putovanje trip3;

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
        trip1 = Putovanje.builder()
                .naziv("Paris Trip")
                .opis("Exploring Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.valueOf(1500.00))
                .build();

        trip2 = Putovanje.builder()
                .naziv("Rome Adventure")
                .opis("Historical Rome")
                .datumPoc(LocalDate.of(2024, 7, 15))
                .datumKraj(LocalDate.of(2024, 7, 25))
                .ukTrosak(BigDecimal.valueOf(2000.00))
                .build();

        trip3 = Putovanje.builder()
                .naziv("London Visit")
                .opis("Business trip to London")
                .datumPoc(LocalDate.of(2024, 5, 1))
                .datumKraj(LocalDate.of(2024, 5, 5))
                .ukTrosak(BigDecimal.valueOf(800.00))
                .build();

        entityManager.persist(trip1);
        entityManager.persist(trip2);
        entityManager.persist(trip3);

        // Create participants - user1 participates in trip1 and trip3
        Sudionik participant1 = Sudionik.builder()
                .putovanje(trip1)
                .korisnik(testUser1)
                .uloga("organizer")
                .build();

        Sudionik participant2 = Sudionik.builder()
                .putovanje(trip3)
                .korisnik(testUser1)
                .uloga("participant")
                .build();

        // user2 participates in trip2
        Sudionik participant3 = Sudionik.builder()
                .putovanje(trip2)
                .korisnik(testUser2)
                .uloga("organizer")
                .build();

        entityManager.persist(participant1);
        entityManager.persist(participant2);
        entityManager.persist(participant3);
        entityManager.flush();
    }

    @Test
    void findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc_WithExistingUser_ShouldReturnTripsOrderedByDate() {
        // When
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(
                testUser1.getKorisnikId());

        // Then
        assertThat(results).hasSize(2);
        // Should be ordered by datumPoc descending (newest first)
        assertThat(results.get(0).getNaziv()).isEqualTo("Paris Trip"); // 2024-06-01
        assertThat(results.get(1).getNaziv()).isEqualTo("London Visit"); // 2024-05-01
    }

    @Test
    void findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc_WithUserHavingOneTrip_ShouldReturnOneTrip() {
        // When
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(
                testUser2.getKorisnikId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNaziv()).isEqualTo("Rome Adventure");
    }

    @Test
    void findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc_WithNonExistingUser_ShouldReturnEmptyList() {
        // When
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(99999);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findById_WithExistingId_ShouldReturnTrip() {
        // When
        var result = tripRepository.findById(trip1.getPutovanjeId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void save_ShouldPersistNewTrip() {
        // Given
        Putovanje newTrip = Putovanje.builder()
                .naziv("Barcelona Trip")
                .opis("Beach and culture")
                .datumPoc(LocalDate.of(2024, 8, 1))
                .datumKraj(LocalDate.of(2024, 8, 10))
                .ukTrosak(BigDecimal.valueOf(1200.00))
                .build();

        // When
        Putovanje saved = tripRepository.save(newTrip);

        // Then
        assertThat(saved.getPutovanjeId()).isNotNull();
        assertThat(tripRepository.findById(saved.getPutovanjeId())).isPresent();
    }
}
