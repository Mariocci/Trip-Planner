package com.tripplanner.business.integration;

import com.tripplanner.business.TestBusinessApplication;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ActivityRepository;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import com.tripplanner.domain.entity.Trosak;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = TestBusinessApplication.class)
@Transactional
@DisplayName("Trip Cascade Delete Integration Tests")
@Tag("integration")
class TripCascadeDeleteIntegrationTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Korisnik organizerUser;
    private Korisnik participantUser;
    private Putovanje trip;
    private Lokacija testLocation;
    private Kategorija testCategory;

    private Integer tripId;
    private Integer organizerSudionikId;
    private Integer participantSudionikId;
    private Integer activity1Id;
    private Integer activity2Id;
    private Integer expense1Id;
    private Integer expense2Id;
    private Integer locationId;
    private Integer categoryId;
    private Integer organizerUserId;
    private Integer participantUserId;

    @BeforeEach
    void setUp() {
        
        organizerUser = userRepository.save(Korisnik.builder()
                .ime("Olivia")
                .prezime("Organizer")
                .email("olivia.organizer@example.com")
                .oauthProvider("google")
                .oauthId("google-cascade-organizer")
                .build());

        participantUser = userRepository.save(Korisnik.builder()
                .ime("Pete")
                .prezime("Participant")
                .email("pete.participant@example.com")
                .oauthProvider("google")
                .oauthId("google-cascade-participant")
                .build());

        
        trip = tripRepository.save(Putovanje.builder()
                .naziv("Cascade Delete Trip")
                .opis("Trip used to verify cascading delete behavior")
                .datumPoc(LocalDate.of(2024, 7, 1))
                .datumKraj(LocalDate.of(2024, 7, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        
        Sudionik organizerParticipant = participantRepository.save(Sudionik.builder()
                .putovanje(trip)
                .korisnik(organizerUser)
                .uloga("organizer")
                .build());

        Sudionik regularParticipant = participantRepository.save(Sudionik.builder()
                .putovanje(trip)
                .korisnik(participantUser)
                .uloga("participant")
                .build());

        
        testLocation = locationRepository.save(Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build());

        testCategory = categoryRepository.save(Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build());

        
        Aktivnost activity1 = activityRepository.save(Aktivnost.builder()
                .naziv("Visit Eiffel Tower")
                .opis("Morning visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 7, 2, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 7, 2, 12, 0))
                .putovanje(trip)
                .lokacija(testLocation)
                .categories(new java.util.ArrayList<>(Arrays.asList(testCategory)))
                .build());

        Aktivnost activity2 = activityRepository.save(Aktivnost.builder()
                .naziv("Seine River Cruise")
                .opis("Evening cruise")
                .datumVrijemePoc(LocalDateTime.of(2024, 7, 3, 19, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 7, 3, 21, 0))
                .putovanje(trip)
                .lokacija(testLocation)
                .categories(new java.util.ArrayList<>(Arrays.asList(testCategory)))
                .build());

        
        Trosak expense1 = expenseRepository.save(Trosak.builder()
                .iznos(new BigDecimal("125.50"))
                .opis("Hotel booking")
                .datum(LocalDate.of(2024, 7, 1))
                .putovanje(trip)
                .build());

        Trosak expense2 = expenseRepository.save(Trosak.builder()
                .iznos(new BigDecimal("74.25"))
                .opis("Train tickets")
                .datum(LocalDate.of(2024, 7, 2))
                .putovanje(trip)
                .build());

        
        tripId = trip.getPutovanjeId();
        organizerUserId = organizerUser.getKorisnikId();
        participantUserId = participantUser.getKorisnikId();
        organizerSudionikId = organizerParticipant.getSudionikId();
        participantSudionikId = regularParticipant.getSudionikId();
        activity1Id = activity1.getAktivnostId();
        activity2Id = activity2.getAktivnostId();
        expense1Id = expense1.getTrosakId();
        expense2Id = expense2.getTrosakId();
        locationId = testLocation.getLokacijaId();
        categoryId = testCategory.getKategorijaId();

        
        
        
        
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("seed data is fully persisted before deletion")
    void seedData_isFullyPersistedBeforeDeletion() {
        
        
        assertThat(tripRepository.findById(tripId)).isPresent();
        assertThat(participantRepository.findByPutovanje_PutovanjeId(tripId)).hasSize(2);
        assertThat(activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId))
                .hasSize(2);
        assertThat(expenseRepository.findByPutovanje_PutovanjeId(tripId)).hasSize(2);
    }

    @Test
    @DisplayName("deleteTrip removes the trip itself from the database")
    void deleteTrip_removesTripFromRepository() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(tripRepository.findById(tripId)).isEmpty();
        assertThat(tripRepository.existsById(tripId)).isFalse();
    }

    @Test
    @DisplayName("deleteTrip cascades to all associated activities")
    void deleteTrip_cascadesToActivities() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(activityRepository.findById(activity1Id)).isEmpty();
        assertThat(activityRepository.findById(activity2Id)).isEmpty();

        
        List<Aktivnost> tripActivities =
                activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId);
        assertThat(tripActivities).isEmpty();
    }

    @Test
    @DisplayName("deleteTrip cascades to all associated expenses")
    void deleteTrip_cascadesToExpenses() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(expenseRepository.findById(expense1Id)).isEmpty();
        assertThat(expenseRepository.findById(expense2Id)).isEmpty();

        
        List<Trosak> tripExpenses = expenseRepository.findByPutovanje_PutovanjeId(tripId);
        assertThat(tripExpenses).isEmpty();
    }

    @Test
    @DisplayName("deleteTrip cascades to all participants (organizer + regular)")
    void deleteTrip_cascadesToParticipants() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(participantRepository.findById(organizerSudionikId)).isEmpty();
        assertThat(participantRepository.findById(participantSudionikId)).isEmpty();

        
        assertThat(participantRepository.findByPutovanje_PutovanjeId(tripId)).isEmpty();
        assertThat(participantRepository.countOrganizersByPutovanjeId(tripId)).isZero();
    }

    @Test
    @DisplayName("deleteTrip removes every record associated with the trip in a single call")
    void deleteTrip_removesAllAssociatedRecordsInSingleCall() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(tripRepository.findById(tripId)).isEmpty();

        
        assertThat(participantRepository.findByPutovanje_PutovanjeId(tripId)).isEmpty();

        
        assertThat(activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId))
                .isEmpty();

        
        assertThat(expenseRepository.findByPutovanje_PutovanjeId(tripId)).isEmpty();
    }

    @Test
    @DisplayName("deleteTrip does not remove referenced users")
    void deleteTrip_doesNotRemoveUsers() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(userRepository.findById(organizerUserId)).isPresent();
        assertThat(userRepository.findById(participantUserId)).isPresent();
    }

    @Test
    @DisplayName("deleteTrip does not remove shared reference data (locations, categories)")
    void deleteTrip_doesNotRemoveLocationsOrCategories() {
        
        tripService.deleteTrip(tripId, organizerUserId);

        entityManager.flush();
        entityManager.clear();

        
        
        assertThat(locationRepository.findById(locationId)).isPresent();
        assertThat(categoryRepository.findById(categoryId)).isPresent();
    }
}
