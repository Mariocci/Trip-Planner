package com.tripplanner.presentation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripplanner.business.service.impl.ActivityServiceImpl;
import com.tripplanner.business.service.impl.TripServiceImpl;
import com.tripplanner.dataaccess.repository.ActivityRepository;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.controller.ActivityController;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ActivityController} wired with the real
 * {@link com.tripplanner.business.service.ActivityService} (via
 * {@link ActivityServiceImpl}) and the real
 * {@link com.tripplanner.business.service.TripService} (via {@link TripServiceImpl}).
 *
 * <p>Repositories are mocked with {@link MockBean}, so the controller and
 * service layers exchange real DTOs and entities while the data layer is
 * stubbed. This exercises the full request-response flow through both the
 * presentation and business logic layers, and verifies that the service-level
 * authorization checks (driven by {@code TripService#isUserParticipant})
 * actually short-circuit the controller's behaviour and prevent unauthorized
 * mutation of the underlying repository.</p>
 *
 * <p>Validates: Requirements 5.1, 5.4, 5.5, 5.8, 5.9</p>
 */
@WebMvcTest(
        controllers = ActivityController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
@Import({ActivityServiceImpl.class, TripServiceImpl.class, GlobalExceptionHandler.class})
@DisplayName("ActivityController + ActivityService Integration Tests")
@Tag("integration")
class ActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityRepository activityRepository;

    @MockBean
    private TripRepository tripRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    // Required by TripServiceImpl
    @MockBean
    private ParticipantRepository participantRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    private static final Integer TRIP_ID = 10;
    private static final Integer USER_ID = 1;
    private static final Integer NON_PARTICIPANT_USER_ID = 999;
    private static final Integer LOCATION_ID = 50;
    private static final Integer ACTIVITY_ID = 100;
    private static final Integer CATEGORY_ID = 1;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private Korisnik organizerUser;
    private Putovanje testTrip;
    private Lokacija testLocation;
    private Kategorija testCategory;
    private Sudionik organizerParticipant;

    @BeforeEach
    void setUp() {
        startDateTime = LocalDateTime.of(2024, 6, 1, 10, 0);
        endDateTime = LocalDateTime.of(2024, 6, 1, 12, 0);

        organizerUser = Korisnik.builder()
                .korisnikId(USER_ID)
                .ime("Alice")
                .prezime("Anderson")
                .email("alice@example.com")
                .oauthProvider("google")
                .oauthId("google-1")
                .build();

        testTrip = Putovanje.builder()
                .putovanjeId(TRIP_ID)
                .naziv("Paris Trip")
                .opis("Trip to Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build();

        testLocation = Lokacija.builder()
                .lokacijaId(LOCATION_ID)
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build();

        testCategory = Kategorija.builder()
                .kategorijaId(CATEGORY_ID)
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build();

        organizerParticipant = Sudionik.builder()
                .sudionikId(500)
                .putovanje(testTrip)
                .korisnik(organizerUser)
                .uloga("organizer")
                .build();
    }

    /**
     * Builds a valid {@link CreateActivityDTO} that lies within the test trip's
     * date range and references the mocked location and category.
     */
    private CreateActivityDTO validCreateDTO() {
        return CreateActivityDTO.builder()
                .naziv("Eiffel Tower Visit")
                .opis("Visit the iconic Eiffel Tower")
                .datumVrijemePoc(startDateTime)
                .datumVrijemeKraj(endDateTime)
                .lokacijaId(LOCATION_ID)
                .categoryIds(Collections.singletonList(CATEGORY_ID))
                .build();
    }

    @Nested
    @DisplayName("POST /api/trips/{tripId}/activities")
    class CreateActivity {

        @Test
        @DisplayName("createActivity_whenUserIsParticipant_persistsActivityAndReturns201")
        void createActivity_whenUserIsParticipant_persistsActivityAndReturns201() throws Exception {
            // Given - user is a participant of the trip (authorization passes)
            when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(testTrip));
            when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(testLocation));
            when(categoryRepository.findAllById(eq(Collections.singletonList(CATEGORY_ID))))
                    .thenReturn(Collections.singletonList(testCategory));

            // The service hands the persisted entity (with a generated id) back to the controller
            when(activityRepository.save(any(Aktivnost.class))).thenAnswer(invocation -> {
                Aktivnost toSave = invocation.getArgument(0);
                toSave.setAktivnostId(ACTIVITY_ID);
                return toSave;
            });

            CreateActivityDTO request = validCreateDTO();

            // When / Then - request flows controller -> service -> mocked repositories
            // and the response is mapped back from the persisted entity.
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.aktivnostId").value(ACTIVITY_ID))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower Visit"))
                    .andExpect(jsonPath("$.opis").value("Visit the iconic Eiffel Tower"))
                    .andExpect(jsonPath("$.location.lokacijaId").value(LOCATION_ID))
                    .andExpect(jsonPath("$.location.naziv").value("Eiffel Tower"))
                    .andExpect(jsonPath("$.location.grad").value("Paris"))
                    .andExpect(jsonPath("$.categories", hasSize(1)))
                    .andExpect(jsonPath("$.categories[0].kategorijaId").value(CATEGORY_ID))
                    .andExpect(jsonPath("$.categories[0].naziv").value("Sightseeing"));

            // And the service correctly orchestrated the data-access layer:
            // 1) authorization check, 2) trip lookup, 3) location lookup,
            // 4) category lookup, 5) save.
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID);
            verify(tripRepository, times(1)).findById(TRIP_ID);
            verify(locationRepository, times(1)).findById(LOCATION_ID);
            verify(categoryRepository, times(1)).findAllById(Collections.singletonList(CATEGORY_ID));

            // Capture the entity passed to save() to verify request-body fields
            // were faithfully propagated through the controller and service.
            ArgumentCaptor<Aktivnost> savedCaptor = ArgumentCaptor.forClass(Aktivnost.class);
            verify(activityRepository, times(1)).save(savedCaptor.capture());

            Aktivnost saved = savedCaptor.getValue();
            assertThat(saved.getNaziv()).isEqualTo("Eiffel Tower Visit");
            assertThat(saved.getOpis()).isEqualTo("Visit the iconic Eiffel Tower");
            assertThat(saved.getDatumVrijemePoc()).isEqualTo(startDateTime);
            assertThat(saved.getDatumVrijemeKraj()).isEqualTo(endDateTime);
            assertThat(saved.getPutovanje()).isNotNull();
            assertThat(saved.getPutovanje().getPutovanjeId()).isEqualTo(TRIP_ID);
            assertThat(saved.getLokacija()).isNotNull();
            assertThat(saved.getLokacija().getLokacijaId()).isEqualTo(LOCATION_ID);
            assertThat(saved.getCategories())
                    .extracting(Kategorija::getKategorijaId)
                    .containsExactly(CATEGORY_ID);
        }

        @Test
        @DisplayName("createActivity_whenUserNotParticipant_returnsErrorAndDoesNotPersist")
        void createActivity_whenUserNotParticipant_returnsErrorAndDoesNotPersist() throws Exception {
            // Given - the participant lookup returns empty, so the real
            // TripService#isUserParticipant returns false and the real
            // ActivityService throws RuntimeException("Access denied: ...").
            when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                    TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            CreateActivityDTO request = validCreateDTO();

            // When / Then - GlobalExceptionHandler maps RuntimeException -> 500
            // with the original "Access denied" message preserved end-to-end.
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            // Authorization was actually consulted
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                            TRIP_ID, NON_PARTICIPANT_USER_ID);

            // And the service short-circuited: no trip/location lookup, no save
            verify(tripRepository, never()).findById(any());
            verify(locationRepository, never()).findById(any());
            verify(categoryRepository, never()).findAllById(anyList());
            verify(activityRepository, never()).save(any(Aktivnost.class));
        }

        @Test
        @DisplayName("createActivity_whenEndBeforeStart_returns400AndDoesNotPersist")
        void createActivity_whenEndBeforeStart_returns400AndDoesNotPersist() throws Exception {
            // Given - user is a participant, but request has end < start
            when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));

            CreateActivityDTO invalidRequest = CreateActivityDTO.builder()
                    .naziv("Eiffel Tower Visit")
                    .opis("Reversed dates")
                    .datumVrijemePoc(endDateTime)        // start = 12:00
                    .datumVrijemeKraj(startDateTime)     // end   = 10:00
                    .lokacijaId(LOCATION_ID)
                    .categoryIds(Collections.emptyList())
                    .build();

            // When / Then - service throws IllegalArgumentException, mapped to 400
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("End datetime must be after start datetime"));

            // Authorization passed, then date validation rejected the request
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID);

            // No persistence side-effects
            verify(tripRepository, never()).findById(any());
            verify(locationRepository, never()).findById(any());
            verify(activityRepository, never()).save(any(Aktivnost.class));
        }

        @Test
        @DisplayName("createActivity_withInvalidRequestBody_returns400AndDoesNotInvokeService")
        void createActivity_withInvalidRequestBody_returns400AndDoesNotInvokeService() throws Exception {
            // Given - request body fails @Valid (blank naziv, null start/end)
            CreateActivityDTO invalid = CreateActivityDTO.builder()
                    .naziv("")
                    .datumVrijemePoc(null)
                    .datumVrijemeKraj(null)
                    .lokacijaId(null)
                    .build();

            // When / Then - the controller's @Valid binding kicks in before the
            // service is reached, so the data layer is never touched.
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(participantRepository, never())
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(any(), any());
            verify(tripRepository, never()).findById(any());
            verify(activityRepository, never()).save(any(Aktivnost.class));
        }

        @Test
        @DisplayName("createActivity_propagatesServiceTripLookupFailure")
        void createActivity_propagatesServiceTripLookupFailure() throws Exception {
            // Given - user authorized, but trip lookup misses
            when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

            CreateActivityDTO request = validCreateDTO();

            // When / Then - service throws RuntimeException("Trip not found"),
            // mapped to 500 by the GlobalExceptionHandler.
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            // Service progressed past auth and date validation, attempted trip lookup,
            // then bailed before location lookup or save.
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, USER_ID);
            verify(tripRepository, times(1)).findById(TRIP_ID);
            verify(locationRepository, never()).findById(any());
            verify(activityRepository, never()).save(any(Aktivnost.class));
        }
    }

    @Nested
    @DisplayName("Authorization checks across endpoints")
    class AuthorizationChecks {

        @Test
        @DisplayName("createActivity_authorizationConsultsParticipantRepositoryBeforeAnyMutation")
        void createActivity_authorizationConsultsParticipantRepositoryBeforeAnyMutation() throws Exception {
            // Given - a non-participant attempts to create an activity
            when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            CreateActivityDTO request = validCreateDTO();

            // When
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            // Then - the participant lookup is the very first repository
            // interaction and the only one the service made before failing.
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID);
            verify(tripRepository, never()).findById(any());
            verify(locationRepository, never()).findById(any());
            verify(categoryRepository, never()).findAllById(anyList());
            verify(activityRepository, never()).save(any(Aktivnost.class));
        }
    }
}
