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


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

        
        Sudionik participant3 = Sudionik.builder()
                .putovanje(trip2)
                .korisnik(testUser2)
                .uloga("organizer")
                .build();

        
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
        
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(
                testUser1.getKorisnikId());

        
        assertThat(results).hasSize(2);
        
        assertThat(results.get(0).getNaziv()).isEqualTo("Paris Trip"); 
        assertThat(results.get(1).getNaziv()).isEqualTo("London Visit"); 
    }

    @Test
    void findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc_WithUserHavingOneTrip_ShouldReturnOneTrip() {
        
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(
                testUser2.getKorisnikId());

        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNaziv()).isEqualTo("Rome Adventure");
    }

    @Test
    void findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc_WithNonExistingUser_ShouldReturnEmptyList() {
        
        List<Putovanje> results = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(99999);

        
        assertThat(results).isEmpty();
    }

    @Test
    void findById_WithExistingId_ShouldReturnTrip() {
        
        var result = tripRepository.findById(trip1.getPutovanjeId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Paris Trip");
    }

    @Test
    void save_ShouldPersistNewTrip() {
        
        Putovanje newTrip = Putovanje.builder()
                .naziv("Barcelona Trip")
                .opis("Beach and culture")
                .datumPoc(LocalDate.of(2024, 8, 1))
                .datumKraj(LocalDate.of(2024, 8, 10))
                .ukTrosak(BigDecimal.valueOf(1200.00))
                .build();

        
        Putovanje saved = tripRepository.save(newTrip);

        
        assertThat(saved.getPutovanjeId()).isNotNull();
        assertThat(tripRepository.findById(saved.getPutovanjeId())).isPresent();
    }

    @Test
    void findAll_ShouldReturnAllTrips() {
        
        List<Putovanje> results = tripRepository.findAll();

        
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Putovanje::getNaziv)
                .containsExactlyInAnyOrder("Paris Trip", "Rome Adventure", "London Visit");
    }

    @Test
    void update_ShouldModifyExistingTrip() {
        
        Putovanje tripToUpdate = tripRepository.findById(trip1.getPutovanjeId()).orElseThrow();
        String newName = "Updated Paris Trip";
        String newDescription = "Updated description";
        BigDecimal newCost = BigDecimal.valueOf(2500.00);

        
        tripToUpdate.setNaziv(newName);
        tripToUpdate.setOpis(newDescription);
        tripToUpdate.setUkTrosak(newCost);
        Putovanje updated = tripRepository.save(tripToUpdate);

        
        assertThat(updated.getNaziv()).isEqualTo(newName);
        assertThat(updated.getOpis()).isEqualTo(newDescription);
        assertThat(updated.getUkTrosak()).isEqualByComparingTo(newCost);

        
        Putovanje retrieved = tripRepository.findById(trip1.getPutovanjeId()).orElseThrow();
        assertThat(retrieved.getNaziv()).isEqualTo(newName);
    }

    @Test
    void delete_ShouldRemoveTripAndCascadeToParticipants() {
        
        Integer tripId = trip1.getPutovanjeId();
        
        
        Putovanje tripToDelete = entityManager.find(Putovanje.class, tripId);
        
        tripToDelete.getParticipants().size();
        
        assertThat(tripRepository.findById(tripId)).isPresent();

        
        tripRepository.delete(tripToDelete);
        entityManager.flush();

        
        assertThat(tripRepository.findById(tripId)).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveTripAndCascadeToParticipants() {
        
        Integer tripId = trip2.getPutovanjeId();
        
        
        Putovanje tripToDelete = entityManager.find(Putovanje.class, tripId);
        tripToDelete.getParticipants().size(); 
        
        assertThat(tripRepository.findById(tripId)).isPresent();

        
        tripRepository.delete(tripToDelete);
        entityManager.flush();

        
        assertThat(tripRepository.findById(tripId)).isEmpty();
    }

    @Test
    void cascadeDelete_ShouldDeleteAssociatedParticipants() {
        
        Integer tripId = trip1.getPutovanjeId();
        
        
        Putovanje tripWithParticipants = entityManager.find(Putovanje.class, tripId);
        
        
        int participantCount = tripWithParticipants.getParticipants().size();
        assertThat(participantCount).as("Trip should have participants before delete").isGreaterThan(0);

        
        tripRepository.delete(tripWithParticipants);
        entityManager.flush();
        entityManager.clear();

        
        assertThat(tripRepository.findById(tripId)).isEmpty();
        
        
        List<Sudionik> remainingParticipants = entityManager.getEntityManager()
                .createQuery("SELECT s FROM Sudionik s WHERE s.putovanje.putovanjeId = :tripId", Sudionik.class)
                .setParameter("tripId", tripId)
                .getResultList();
        assertThat(remainingParticipants).isEmpty();
    }

    @Test
    void entityRelationships_ShouldLoadParticipantsCorrectly() {
        
        Putovanje trip = entityManager.find(Putovanje.class, trip1.getPutovanjeId());
        
        
        int participantSize = trip.getParticipants().size();

        
        assertThat(trip.getParticipants()).as("Trip should have participants").isNotEmpty();
        assertThat(trip.getParticipants()).hasSize(1);
        
        Sudionik participant = trip.getParticipants().get(0);
        assertThat(participant.getKorisnik()).isNotNull();
        assertThat(participant.getKorisnik().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(participant.getUloga()).isEqualTo("organizer");
    }

    @Test
    void entityRelationships_ShouldLoadActivitiesAndExpenses() {
        
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

        
        Putovanje retrieved = tripRepository.findById(tripWithRelations.getPutovanjeId()).orElseThrow();

        
        assertThat(retrieved.getActivities()).isNotNull();
        assertThat(retrieved.getExpenses()).isNotNull();
        assertThat(retrieved.getParticipants()).isNotNull();
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        
        var result = tripRepository.findById(99999);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> tripRepository.findById(null)
        );
    }

    @Test
    void save_WithNullValues_ShouldPersistTrip() {
        
        Putovanje minimalTrip = Putovanje.builder()
                .naziv("Minimal Trip")
                .datumPoc(LocalDate.of(2024, 10, 1))
                .datumKraj(LocalDate.of(2024, 10, 5))
                .build();

        
        Putovanje saved = tripRepository.save(minimalTrip);

        
        assertThat(saved.getPutovanjeId()).isNotNull();
        assertThat(saved.getOpis()).isNull();
        assertThat(saved.getUkTrosak()).isNull();
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Sudionik")
                .executeUpdate();
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Putovanje")
                .executeUpdate();
        entityManager.flush();

        
        List<Putovanje> results = tripRepository.findAll();

        
        assertThat(results).isEmpty();
    }
}
