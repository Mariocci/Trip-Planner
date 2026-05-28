package com.tripplanner.business.integration;

import com.tripplanner.business.TestBusinessApplication;
import com.tripplanner.business.service.ActivityService;
import com.tripplanner.dataaccess.repository.ActivityRepository;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(classes = TestBusinessApplication.class)
@Transactional
@Tag("integration")
class ActivityServiceIntegrationTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Korisnik organizerUser;
    private Putovanje testTrip;
    private Lokacija testLocation;
    private Kategorija testCategory1;
    private Kategorija testCategory2;

    @BeforeEach
    void setUp() {
        
        organizerUser = userRepository.save(Korisnik.builder()
                .ime("Alice")
                .prezime("Anderson")
                .email("alice.integration@example.com")
                .oauthProvider("google")
                .oauthId("google-integration-1")
                .build());

        
        testTrip = tripRepository.save(Putovanje.builder()
                .naziv("Integration Test Trip")
                .opis("Trip for activity integration tests")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        
        participantRepository.save(Sudionik.builder()
                .putovanje(testTrip)
                .korisnik(organizerUser)
                .uloga("organizer")
                .build());

        
        testLocation = locationRepository.save(Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build());

        
        testCategory1 = categoryRepository.save(Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build());

        testCategory2 = categoryRepository.save(Kategorija.builder()
                .naziv("Culture")
                .opis("Cultural activities")
                .build());

        
        entityManager.flush();
    }

    @Test
    void createActivity_withValidData_persistsActivityInRepository() {
        
        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Visit Eiffel Tower")
                .opis("Morning visit to the Eiffel Tower")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 2, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 2, 12, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Arrays.asList(testCategory1.getKategorijaId()))
                .build();

        
        ActivityResponseDTO response = activityService.createActivity(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), createDTO);

        
        entityManager.flush();
        entityManager.clear();

        
        assertThat(response).isNotNull();
        assertThat(response.getAktivnostId()).isNotNull();
        assertThat(response.getNaziv()).isEqualTo("Visit Eiffel Tower");
        assertThat(response.getOpis()).isEqualTo("Morning visit to the Eiffel Tower");

        
        Optional<Aktivnost> persistedOpt = activityRepository.findById(response.getAktivnostId());
        assertThat(persistedOpt).isPresent();

        Aktivnost persisted = persistedOpt.get();
        assertThat(persisted.getNaziv()).isEqualTo("Visit Eiffel Tower");
        assertThat(persisted.getDatumVrijemePoc()).isEqualTo(LocalDateTime.of(2024, 6, 2, 9, 0));
        assertThat(persisted.getDatumVrijemeKraj()).isEqualTo(LocalDateTime.of(2024, 6, 2, 12, 0));
        assertThat(persisted.getPutovanje()).isNotNull();
        assertThat(persisted.getPutovanje().getPutovanjeId()).isEqualTo(testTrip.getPutovanjeId());
    }

    @Test
    void createActivity_withValidData_associatesLocationCorrectly() {
        
        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Louvre Museum Tour")
                .opis("Afternoon at the Louvre")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 3, 14, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 3, 18, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Collections.emptyList())
                .build();

        
        ActivityResponseDTO response = activityService.createActivity(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), createDTO);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(response.getLocation()).isNotNull();
        assertThat(response.getLocation().getLokacijaId()).isEqualTo(testLocation.getLokacijaId());
        assertThat(response.getLocation().getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(response.getLocation().getAdresa()).isEqualTo("Champ de Mars");
        assertThat(response.getLocation().getGrad()).isEqualTo("Paris");
        assertThat(response.getLocation().getDrzava()).isEqualTo("France");

        
        Aktivnost persisted = activityRepository.findById(response.getAktivnostId()).orElseThrow();
        assertThat(persisted.getLokacija()).isNotNull();
        assertThat(persisted.getLokacija().getLokacijaId()).isEqualTo(testLocation.getLokacijaId());
        assertThat(persisted.getLokacija().getNaziv()).isEqualTo("Eiffel Tower");
    }

    @Test
    void createActivity_withMultipleCategories_associatesAllCategoriesCorrectly() {
        
        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Cultural Walking Tour")
                .opis("Guided cultural walking tour")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 4, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 4, 13, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Arrays.asList(
                        testCategory1.getKategorijaId(),
                        testCategory2.getKategorijaId()))
                .build();

        
        ActivityResponseDTO response = activityService.createActivity(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), createDTO);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(response.getCategories()).hasSize(2);
        assertThat(response.getCategories())
                .extracting(c -> c.getNaziv())
                .containsExactlyInAnyOrder("Sightseeing", "Culture");

        
        Aktivnost persisted = activityRepository.findById(response.getAktivnostId()).orElseThrow();
        assertThat(persisted.getCategories()).hasSize(2);
        assertThat(persisted.getCategories())
                .extracting(Kategorija::getKategorijaId)
                .containsExactlyInAnyOrder(
                        testCategory1.getKategorijaId(),
                        testCategory2.getKategorijaId());
    }

    @Test
    void createActivity_withoutCategories_persistsActivityWithEmptyCategories() {
        
        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Free Time")
                .opis("Unstructured free time")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 5, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 5, 11, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(null)
                .build();

        
        ActivityResponseDTO response = activityService.createActivity(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), createDTO);

        entityManager.flush();
        entityManager.clear();

        
        assertThat(response.getCategories()).isEmpty();

        Aktivnost persisted = activityRepository.findById(response.getAktivnostId()).orElseThrow();
        assertThat(persisted.getCategories()).isEmpty();
        assertThat(persisted.getLokacija()).isNotNull();
        assertThat(persisted.getLokacija().getLokacijaId()).isEqualTo(testLocation.getLokacijaId());
    }

    @Test
    void createActivity_persistsActivityRetrievableViaCustomQuery() {
        
        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Seine River Cruise")
                .opis("Evening river cruise")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 19, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 21, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Arrays.asList(testCategory1.getKategorijaId()))
                .build();

        
        activityService.createActivity(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), createDTO);

        entityManager.flush();
        entityManager.clear();

        
        List<Aktivnost> tripActivities =
                activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(
                        testTrip.getPutovanjeId());

        assertThat(tripActivities).hasSize(1);
        Aktivnost persisted = tripActivities.get(0);
        assertThat(persisted.getNaziv()).isEqualTo("Seine River Cruise");
        assertThat(persisted.getLokacija().getLokacijaId()).isEqualTo(testLocation.getLokacijaId());
        assertThat(persisted.getCategories())
                .extracting(Kategorija::getKategorijaId)
                .containsExactly(testCategory1.getKategorijaId());
    }

    @Test
    void createActivity_whenUserNotParticipant_doesNotPersistActivity() {
        
        Korisnik outsider = userRepository.save(Korisnik.builder()
                .ime("Bob")
                .prezime("Outsider")
                .email("bob.outsider@example.com")
                .oauthProvider("google")
                .oauthId("google-outsider-1")
                .build());
        entityManager.flush();

        CreateActivityDTO createDTO = CreateActivityDTO.builder()
                .naziv("Unauthorized Activity")
                .opis("Should not be created")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 7, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 7, 12, 0))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Arrays.asList(testCategory1.getKategorijaId()))
                .build();

        
        assertThatThrownBy(() -> activityService.createActivity(
                testTrip.getPutovanjeId(), outsider.getKorisnikId(), createDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        
        List<Aktivnost> tripActivities =
                activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(
                        testTrip.getPutovanjeId());
        assertThat(tripActivities).isEmpty();
    }
}
