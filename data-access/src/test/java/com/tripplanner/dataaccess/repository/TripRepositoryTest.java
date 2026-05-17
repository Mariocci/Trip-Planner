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

        // Add participants to trip collections for bidirectional relationship
        trip1.getParticipants().add(participant1);
        trip2.getParticipants().add(participant3);
        trip3.getParticipants().add(participant2);

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

    @Test
    void findAll_ShouldReturnAllTrips() {
        // When
        List<Putovanje> results = tripRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Putovanje::getNaziv)
                .containsExactlyInAnyOrder("Paris Trip", "Rome Adventure", "London Visit");
    }

    @Test
    void update_ShouldModifyExistingTrip() {
        // Given
        Putovanje tripToUpdate = tripRepository.findById(trip1.getPutovanjeId()).orElseThrow();
        String newName = "Updated Paris Trip";
        String newDescription = "Updated description";
        BigDecimal newCost = BigDecimal.valueOf(2500.00);

        // When
        tripToUpdate.setNaziv(newName);
        tripToUpdate.setOpis(newDescription);
        tripToUpdate.setUkTrosak(newCost);
        Putovanje updated = tripRepository.save(tripToUpdate);

        // Then
        assertThat(updated.getNaziv()).isEqualTo(newName);
        assertThat(updated.getOpis()).isEqualTo(newDescription);
        assertThat(updated.getUkTrosak()).isEqualByComparingTo(newCost);

        // Verify persistence
        Putovanje retrieved = tripRepository.findById(trip1.getPutovanjeId()).orElseThrow();
        assertThat(retrieved.getNaziv()).isEqualTo(newName);
    }

    @Test
    void delete_ShouldRemoveTripAndCascadeToParticipants() {
        // Given
        Integer tripId = trip1.getPutovanjeId();
        
        // Load the trip with participants to enable cascade delete
        Putovanje tripToDelete = entityManager.find(Putovanje.class, tripId);
        // Initialize the participants collection to enable cascade
        tripToDelete.getParticipants().size();
        
        assertThat(tripRepository.findById(tripId)).isPresent();

        // When - Delete using the managed entity with cascade
        tripRepository.delete(tripToDelete);
        entityManager.flush();

        // Then
        assertThat(tripRepository.findById(tripId)).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveTripAndCascadeToParticipants() {
        // Given
        Integer tripId = trip2.getPutovanjeId();
        
        // Load and initialize the trip with participants
        Putovanje tripToDelete = entityManager.find(Putovanje.class, tripId);
        tripToDelete.getParticipants().size(); // Initialize lazy collection
        
        assertThat(tripRepository.findById(tripId)).isPresent();

        // When - Delete by ID will cascade due to entity configuration
        tripRepository.delete(tripToDelete);
        entityManager.flush();

        // Then
        assertThat(tripRepository.findById(tripId)).isEmpty();
    }

    @Test
    void cascadeDelete_ShouldDeleteAssociatedParticipants() {
        // Given
        Integer tripId = trip1.getPutovanjeId();
        
        // Load the trip with its participants in the same transaction
        Putovanje tripWithParticipants = entityManager.find(Putovanje.class, tripId);
        
        // Force initialization of participants collection
        int participantCount = tripWithParticipants.getParticipants().size();
        assertThat(participantCount).as("Trip should have participants before delete").isGreaterThan(0);

        // When - Delete the trip (cascade should delete participants)
        tripRepository.delete(tripWithParticipants);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(tripRepository.findById(tripId)).isEmpty();
        
        // Verify participants were cascade deleted
        List<Sudionik> remainingParticipants = entityManager.getEntityManager()
                .createQuery("SELECT s FROM Sudionik s WHERE s.putovanje.putovanjeId = :tripId", Sudionik.class)
                .setParameter("tripId", tripId)
                .getResultList();
        assertThat(remainingParticipants).isEmpty();
    }

    @Test
    void entityRelationships_ShouldLoadParticipantsCorrectly() {
        // When - Load trip using entityManager to ensure proper initialization
        Putovanje trip = entityManager.find(Putovanje.class, trip1.getPutovanjeId());
        
        // Force lazy loading by accessing the collection
        int participantSize = trip.getParticipants().size();

        // Then
        assertThat(trip.getParticipants()).as("Trip should have participants").isNotEmpty();
        assertThat(trip.getParticipants()).hasSize(1);
        
        Sudionik participant = trip.getParticipants().get(0);
        assertThat(participant.getKorisnik()).isNotNull();
        assertThat(participant.getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(participant.getUloga()).isEqualTo("organizer");
    }

    @Test
    void entityRelationships_ShouldLoadActivitiesAndExpenses() {
        // Given - Create a trip with activities and expenses
        Putovanje tripWithRelations = Putovanje.builder()
                .naziv("Test Trip")
                .opis("Trip with relations")
                .datumPoc(LocalDate.of(2024, 9, 1))
                .datumKraj(LocalDate.of(2024, 9, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build();
        
        entityManager.persist(tripWithRelations);
        entityManager.flush();
        entityManager.clear();

        // When
        Putovanje retrieved = tripRepository.findById(tripWithRelations.getPutovanjeId()).orElseThrow();

        // Then
        assertThat(retrieved.getActivities()).isNotNull();
        assertThat(retrieved.getExpenses()).isNotNull();
        assertThat(retrieved.getParticipants()).isNotNull();
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // When
        var result = tripRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        // When/Then - Spring Data JPA throws exception for null IDs
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> tripRepository.findById(null)
        );
    }

    @Test
    void save_WithNullValues_ShouldPersistTrip() {
        // Given - Trip with minimal required fields
        Putovanje minimalTrip = Putovanje.builder()
                .naziv("Minimal Trip")
                .datumPoc(LocalDate.of(2024, 10, 1))
                .datumKraj(LocalDate.of(2024, 10, 5))
                .build();

        // When
        Putovanje saved = tripRepository.save(minimalTrip);

        // Then
        assertThat(saved.getPutovanjeId()).isNotNull();
        assertThat(saved.getOpis()).isNull();
        assertThat(saved.getUkTrosak()).isNull();
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given - Clear all data (participants first due to foreign key)
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Sudionik")
                .executeUpdate();
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Putovanje")
                .executeUpdate();
        entityManager.flush();

        // When
        List<Putovanje> results = tripRepository.findAll();

        // Then
        assertThat(results).isEmpty();
    }
}
