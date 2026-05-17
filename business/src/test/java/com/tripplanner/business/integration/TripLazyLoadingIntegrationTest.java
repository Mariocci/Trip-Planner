package com.tripplanner.business.integration;

import com.tripplanner.business.TestBusinessApplication;
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
import org.hibernate.Hibernate;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that verify lazy-loaded JPA relationships on the
 * {@link Putovanje} aggregate behave correctly when accessed inside an open
 * Hibernate session.
 *
 * <p>The {@link Putovanje} entity exposes three {@link jakarta.persistence.OneToMany}
 * collections - {@code activities}, {@code expenses}, and {@code participants} -
 * which default to {@code FetchType.LAZY} in JPA. Likewise, {@link Aktivnost}
 * declares {@code putovanje}, {@code lokacija} as lazy {@code @ManyToOne} and
 * {@code categories} as a (lazy by default) {@code @ManyToMany}, and
 * {@link Sudionik} declares both {@code putovanje} and {@code korisnik} as lazy.</p>
 *
 * <p>This test boots the full Spring context against an H2 in-memory database via
 * {@code @SpringBootTest} and uses {@code @Transactional} to keep a single
 * persistence context open for the duration of each test, so lazy associations
 * can be initialized on demand. It seeds a trip with two participants, two
 * activities (each with a location and a category), and two expenses, then:</p>
 * <ul>
 *   <li>Confirms lazy collections on {@code Putovanje} start uninitialized after
 *       a fresh repository load.</li>
 *   <li>Triggers initialization and verifies the participants, activities, and
 *       expenses are loaded with the correct contents.</li>
 *   <li>Confirms lazy {@code @ManyToOne} associations on the children resolve
 *       to the correct parent / reference entities.</li>
 * </ul>
 *
 * <p>Validates Requirements: 4.1, 4.6, 4.9, 4.10</p>
 */
@SpringBootTest(classes = TestBusinessApplication.class)
@Transactional
@DisplayName("Trip Lazy Loading Integration Tests")
@Tag("integration")
class TripLazyLoadingIntegrationTest {

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

    private Integer tripId;
    private Integer organizerUserId;
    private Integer participantUserId;
    private Integer locationId;
    private Integer categoryId;
    private Integer activity1Id;
    private Integer activity2Id;
    private Integer expense1Id;
    private Integer expense2Id;

    @BeforeEach
    void setUp() {
        // Two users: an organizer and a regular participant.
        Korisnik organizerUser = userRepository.save(Korisnik.builder()
                .ime("Lana")
                .prezime("Lazy")
                .email("lana.lazy@example.com")
                .oauthProvider("google")
                .oauthId("google-lazy-organizer")
                .build());

        Korisnik participantUser = userRepository.save(Korisnik.builder()
                .ime("Liam")
                .prezime("Lazy")
                .email("liam.lazy@example.com")
                .oauthProvider("google")
                .oauthId("google-lazy-participant")
                .build());

        // The trip itself.
        Putovanje trip = tripRepository.save(Putovanje.builder()
                .naziv("Lazy Loading Trip")
                .opis("Trip used to verify lazy initialization of collections")
                .datumPoc(LocalDate.of(2024, 8, 1))
                .datumKraj(LocalDate.of(2024, 8, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        // Two participants linked to the trip.
        Sudionik organizerParticipant = participantRepository.save(Sudionik.builder()
                .putovanje(trip)
                .korisnik(organizerUser)
                .uloga("organizer")
                .build());

        participantRepository.save(Sudionik.builder()
                .putovanje(trip)
                .korisnik(participantUser)
                .uloga("participant")
                .build());

        // Reference data shared between activities.
        Lokacija testLocation = locationRepository.save(Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build());

        Kategorija testCategory = categoryRepository.save(Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build());

        // Two activities tied to the trip.
        Aktivnost activity1 = activityRepository.save(Aktivnost.builder()
                .naziv("Visit Eiffel Tower")
                .opis("Morning visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 8, 2, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 8, 2, 12, 0))
                .putovanje(trip)
                .lokacija(testLocation)
                .categories(new java.util.ArrayList<>(Arrays.asList(testCategory)))
                .build());

        Aktivnost activity2 = activityRepository.save(Aktivnost.builder()
                .naziv("Seine River Cruise")
                .opis("Evening cruise")
                .datumVrijemePoc(LocalDateTime.of(2024, 8, 3, 19, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 8, 3, 21, 0))
                .putovanje(trip)
                .lokacija(testLocation)
                .categories(new java.util.ArrayList<>(Arrays.asList(testCategory)))
                .build());

        // Two expenses tied to the trip.
        Trosak expense1 = expenseRepository.save(Trosak.builder()
                .iznos(new BigDecimal("125.50"))
                .opis("Hotel booking")
                .datum(LocalDate.of(2024, 8, 1))
                .putovanje(trip)
                .build());

        Trosak expense2 = expenseRepository.save(Trosak.builder()
                .iznos(new BigDecimal("74.25"))
                .opis("Train tickets")
                .datum(LocalDate.of(2024, 8, 2))
                .putovanje(trip)
                .build());

        tripId = trip.getPutovanjeId();
        organizerUserId = organizerUser.getKorisnikId();
        participantUserId = participantUser.getKorisnikId();
        locationId = testLocation.getLokacijaId();
        categoryId = testCategory.getKategorijaId();
        activity1Id = activity1.getAktivnostId();
        activity2Id = activity2.getAktivnostId();
        expense1Id = expense1.getTrosakId();
        expense2Id = expense2.getTrosakId();

        // Identify org participant id implicitly by using the saved entity.
        assertThat(organizerParticipant.getSudionikId()).isNotNull();

        // Flush all seed inserts to the database, then clear the persistence
        // context so that subsequent repository.findById calls produce fresh
        // entities with uninitialized lazy proxies. This is essential for
        // exercising lazy initialization rather than reading from the
        // first-level cache.
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Putovanje lazy collections are uninitialized immediately after load")
    void lazyCollections_areUninitialized_afterFreshLoad() {
        // When - load the trip without touching its collections.
        Putovanje loaded = tripRepository.findById(tripId).orElseThrow();

        // Then - all three OneToMany collections should be lazy proxies that
        // are not yet initialized.
        assertThat(Hibernate.isInitialized(loaded.getActivities()))
                .as("activities collection should start uninitialized")
                .isFalse();
        assertThat(Hibernate.isInitialized(loaded.getExpenses()))
                .as("expenses collection should start uninitialized")
                .isFalse();
        assertThat(Hibernate.isInitialized(loaded.getParticipants()))
                .as("participants collection should start uninitialized")
                .isFalse();
    }

    @Test
    @DisplayName("Putovanje.participants initializes correctly with both participants")
    void participants_initializeCorrectly_whenAccessed() {
        // Given - a freshly loaded trip whose participants collection is lazy.
        Putovanje loaded = tripRepository.findById(tripId).orElseThrow();
        assertThat(Hibernate.isInitialized(loaded.getParticipants())).isFalse();

        // When - explicitly initialize the lazy participants collection.
        Hibernate.initialize(loaded.getParticipants());

        // Then - the proxy is now initialized and contains both seeded participants.
        assertThat(Hibernate.isInitialized(loaded.getParticipants())).isTrue();
        assertThat(loaded.getParticipants()).hasSize(2);
        assertThat(loaded.getParticipants())
                .extracting(Sudionik::getUloga)
                .containsExactlyInAnyOrder("organizer", "participant");

        // And the lazy ManyToOne back-reference to the user resolves correctly
        // for each participant.
        assertThat(loaded.getParticipants())
                .allSatisfy(p -> {
                    assertThat(p.getKorisnik()).isNotNull();
                    assertThat(p.getKorisnik().getKorisnikId()).isNotNull();
                });
        assertThat(loaded.getParticipants())
                .extracting(p -> p.getKorisnik().getKorisnikId())
                .containsExactlyInAnyOrder(organizerUserId, participantUserId);
    }

    @Test
    @DisplayName("Putovanje.activities initializes correctly with both activities")
    void activities_initializeCorrectly_whenAccessed() {
        // Given - a freshly loaded trip whose activities collection is lazy.
        Putovanje loaded = tripRepository.findById(tripId).orElseThrow();
        assertThat(Hibernate.isInitialized(loaded.getActivities())).isFalse();

        // When - explicitly initialize the lazy activities collection.
        Hibernate.initialize(loaded.getActivities());

        // Then - the proxy is now initialized and contains both seeded activities.
        assertThat(Hibernate.isInitialized(loaded.getActivities())).isTrue();
        assertThat(loaded.getActivities()).hasSize(2);
        assertThat(loaded.getActivities())
                .extracting(Aktivnost::getAktivnostId)
                .containsExactlyInAnyOrder(activity1Id, activity2Id);
        assertThat(loaded.getActivities())
                .extracting(Aktivnost::getNaziv)
                .containsExactlyInAnyOrder("Visit Eiffel Tower", "Seine River Cruise");
    }

    @Test
    @DisplayName("Putovanje.expenses initializes correctly with both expenses")
    void expenses_initializeCorrectly_whenAccessed() {
        // Given - a freshly loaded trip whose expenses collection is lazy.
        Putovanje loaded = tripRepository.findById(tripId).orElseThrow();
        assertThat(Hibernate.isInitialized(loaded.getExpenses())).isFalse();

        // When - explicitly initialize the lazy expenses collection.
        Hibernate.initialize(loaded.getExpenses());

        // Then - the proxy is now initialized and contains both seeded expenses.
        assertThat(Hibernate.isInitialized(loaded.getExpenses())).isTrue();
        assertThat(loaded.getExpenses()).hasSize(2);
        assertThat(loaded.getExpenses())
                .extracting(Trosak::getTrosakId)
                .containsExactlyInAnyOrder(expense1Id, expense2Id);
        assertThat(loaded.getExpenses())
                .extracting(Trosak::getIznos)
                .containsExactlyInAnyOrder(new BigDecimal("125.50"), new BigDecimal("74.25"));
    }

    @Test
    @DisplayName("Aktivnost lazy ManyToOne associations resolve to seeded trip and location")
    void activityLazyManyToOne_resolvesCorrectly() {
        // Given - load an activity directly, which should give us a managed
        // entity with lazy putovanje and lokacija proxies.
        Aktivnost activity = activityRepository.findById(activity1Id).orElseThrow();

        // The lazy ManyToOne references should not be initialized yet.
        assertThat(Hibernate.isInitialized(activity.getPutovanje()))
                .as("activity.putovanje should start uninitialized")
                .isFalse();
        assertThat(Hibernate.isInitialized(activity.getLokacija()))
                .as("activity.lokacija should start uninitialized")
                .isFalse();

        // When - touch the lazy associations.
        Hibernate.initialize(activity.getPutovanje());
        Hibernate.initialize(activity.getLokacija());

        // Then - the proxies are now initialized and resolve to the seeded
        // parent trip and shared location.
        assertThat(Hibernate.isInitialized(activity.getPutovanje())).isTrue();
        assertThat(activity.getPutovanje().getPutovanjeId()).isEqualTo(tripId);
        assertThat(activity.getPutovanje().getNaziv()).isEqualTo("Lazy Loading Trip");

        assertThat(Hibernate.isInitialized(activity.getLokacija())).isTrue();
        assertThat(activity.getLokacija().getLokacijaId()).isEqualTo(locationId);
        assertThat(activity.getLokacija().getNaziv()).isEqualTo("Eiffel Tower");
    }

    @Test
    @DisplayName("Aktivnost.categories ManyToMany initializes lazily with seeded category")
    void activityCategories_initializeCorrectly_whenAccessed() {
        // Given - load an activity; its categories collection should be lazy.
        Aktivnost activity = activityRepository.findById(activity1Id).orElseThrow();
        assertThat(Hibernate.isInitialized(activity.getCategories()))
                .as("activity.categories should start uninitialized")
                .isFalse();

        // When - initialize the lazy categories collection.
        Hibernate.initialize(activity.getCategories());

        // Then - the categories collection contains the seeded category.
        assertThat(Hibernate.isInitialized(activity.getCategories())).isTrue();
        assertThat(activity.getCategories()).hasSize(1);
        assertThat(activity.getCategories())
                .extracting(Kategorija::getKategorijaId)
                .containsExactly(categoryId);
        assertThat(activity.getCategories())
                .extracting(Kategorija::getNaziv)
                .containsExactly("Sightseeing");
    }

    @Test
    @DisplayName("Sudionik lazy ManyToOne associations resolve to seeded trip and user")
    void participantLazyManyToOne_resolvesCorrectly() {
        // Given - load all participants for the trip.
        java.util.List<Sudionik> participants =
                participantRepository.findByPutovanje_PutovanjeId(tripId);
        assertThat(participants).hasSize(2);

        // When / Then - for each participant, the lazy putovanje and korisnik
        // associations resolve to the seeded entities.
        for (Sudionik participant : participants) {
            // Lazy associations may already be implicit Hibernate proxies; either
            // way, dereferencing them should produce correct ids.
            assertThat(participant.getPutovanje()).isNotNull();
            assertThat(participant.getPutovanje().getPutovanjeId()).isEqualTo(tripId);

            assertThat(participant.getKorisnik()).isNotNull();
            assertThat(participant.getKorisnik().getKorisnikId())
                    .isIn(organizerUserId, participantUserId);
        }
    }

    @Test
    @DisplayName("Loaded trip exposes both participants and activities through lazy collections")
    void loadedTrip_exposesParticipantsAndActivities_viaLazyCollections() {
        // Given - load the trip and its lazy collections.
        Putovanje loaded = tripRepository.findById(tripId).orElseThrow();

        // When - initialize both relevant lazy collections.
        Hibernate.initialize(loaded.getParticipants());
        Hibernate.initialize(loaded.getActivities());

        // Then - participants are loaded correctly.
        assertThat(loaded.getParticipants()).hasSize(2);
        assertThat(loaded.getParticipants())
                .extracting(p -> p.getKorisnik().getKorisnikId())
                .containsExactlyInAnyOrder(organizerUserId, participantUserId);

        // And activities are loaded correctly.
        assertThat(loaded.getActivities()).hasSize(2);
        assertThat(loaded.getActivities())
                .extracting(Aktivnost::getAktivnostId)
                .containsExactlyInAnyOrder(activity1Id, activity2Id);
        assertThat(loaded.getActivities())
                .allSatisfy(a -> assertThat(a.getPutovanje().getPutovanjeId()).isEqualTo(tripId));
    }
}
