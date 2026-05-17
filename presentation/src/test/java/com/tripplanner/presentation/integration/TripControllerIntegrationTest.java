package com.tripplanner.presentation.integration;

import com.tripplanner.business.service.impl.TripServiceImpl;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import com.tripplanner.presentation.base.ControllerTestBase;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.controller.TripController;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test wiring {@link TripController} together with the real
 * {@link TripServiceImpl} while mocking the underlying repositories.
 *
 * <p>This test exercises the full request-response flow through the
 * controller and service layers, isolating only the data access layer.
 * The intent is to verify that authorization checks implemented in the
 * service are correctly triggered by HTTP requests, that organizer-only
 * operations reject requests from non-organizers, and that the request
 * payload submitted to the controller is correctly translated into
 * persistence calls by the service.</p>
 *
 * <p>Security filters and the OAuth2 resource server are excluded so the
 * tests can target authorization logic at the service layer rather than
 * Spring Security. Authorization in {@link TripServiceImpl} is implemented
 * by throwing {@link RuntimeException} with an "Access denied" message,
 * which the {@link GlobalExceptionHandler} maps to HTTP 500. That mapping
 * is the production behavior and is asserted here.</p>
 *
 * <p>Validates Requirements: 5.1, 5.2, 5.5, 5.6, 5.9</p>
 */
@WebMvcTest(
        controllers = TripController.class,
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
@Import({TripServiceImpl.class, GlobalExceptionHandler.class})
@DisplayName("TripController + TripService Integration Tests")
@Tag("integration")
class TripControllerIntegrationTest extends ControllerTestBase {

    @MockBean
    private TripRepository tripRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ParticipantRepository participantRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    private static final Integer TRIP_ID = 100;
    private static final Integer ORGANIZER_USER_ID = 10;
    private static final Integer PARTICIPANT_USER_ID = 20;
    private static final Integer NON_PARTICIPANT_USER_ID = 99;

    private LocalDate startDate;
    private LocalDate endDate;

    private Korisnik organizerUser;
    private Putovanje persistedTrip;
    private Sudionik organizerParticipant;
    private Sudionik regularParticipant;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 6, 1);
        endDate = LocalDate.of(2024, 6, 10);

        organizerUser = Korisnik.builder()
                .korisnikId(ORGANIZER_USER_ID)
                .ime("Olivia")
                .prezime("Organizer")
                .email("olivia.organizer@example.com")
                .oauthProvider("google")
                .oauthId("google-organizer")
                .build();

        persistedTrip = Putovanje.builder()
                .putovanjeId(TRIP_ID)
                .naziv("Paris Trip")
                .opis("Spring vacation in Paris")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .ukTrosak(BigDecimal.ZERO)
                .build();

        organizerParticipant = Sudionik.builder()
                .sudionikId(1)
                .putovanje(persistedTrip)
                .korisnik(organizerUser)
                .uloga("organizer")
                .build();

        regularParticipant = Sudionik.builder()
                .sudionikId(2)
                .putovanje(persistedTrip)
                .korisnik(Korisnik.builder().korisnikId(PARTICIPANT_USER_ID).build())
                .uloga("participant")
                .build();
    }

    // ---------------------------------------------------------------------
    // Trip creation flow - controller -> real service -> mocked repositories
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/trips - real service flow")
    class CreateTripFlow {

        @Test
        @DisplayName("createTrip persists trip and auto-adds creator as organizer")
        void createTrip_withValidRequest_persistsTripAndCreatesOrganizerParticipant() throws Exception {
            // Given - the user creating the trip exists
            when(userRepository.findById(ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerUser));

            // The trip repository assigns an ID on save
            when(tripRepository.save(any(Putovanje.class)))
                    .thenAnswer(invocation -> {
                        Putovanje p = invocation.getArgument(0);
                        p.setPutovanjeId(TRIP_ID);
                        return p;
                    });

            // Saving the organizer participant returns the saved entity
            when(participantRepository.save(any(Sudionik.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // mapToResponseDTO queries the participant list to compute count
            when(participantRepository.findByPutovanje_PutovanjeId(TRIP_ID))
                    .thenReturn(Arrays.asList(organizerParticipant));

            CreateTripDTO request = CreateTripDTO.builder()
                    .naziv("Paris Trip")
                    .opis("Spring vacation in Paris")
                    .datumPoc(startDate)
                    .datumKraj(endDate)
                    .build();

            // When / Then - HTTP 201 with the created trip in the body
            mockMvc.perform(post("/api/trips")
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Paris Trip"))
                    .andExpect(jsonPath("$.opis").value("Spring vacation in Paris"))
                    .andExpect(jsonPath("$.datumPoc").value("2024-06-01"))
                    .andExpect(jsonPath("$.datumKraj").value("2024-06-10"))
                    .andExpect(jsonPath("$.ukTrosak").value(0))
                    .andExpect(jsonPath("$.participantCount").value(1));

            // Verify the real service flowed through: it saved the trip with the
            // submitted fields and the initial total expense set to zero.
            ArgumentCaptor<Putovanje> tripCaptor = ArgumentCaptor.forClass(Putovanje.class);
            verify(tripRepository, times(1)).save(tripCaptor.capture());
            Putovanje savedTrip = tripCaptor.getValue();
            assertThat(savedTrip.getNaziv()).isEqualTo("Paris Trip");
            assertThat(savedTrip.getOpis()).isEqualTo("Spring vacation in Paris");
            assertThat(savedTrip.getDatumPoc()).isEqualTo(startDate);
            assertThat(savedTrip.getDatumKraj()).isEqualTo(endDate);
            assertThat(savedTrip.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);

            // Verify the creator was automatically added as an organizer.
            ArgumentCaptor<Sudionik> participantCaptor = ArgumentCaptor.forClass(Sudionik.class);
            verify(participantRepository, times(1)).save(participantCaptor.capture());
            Sudionik savedOrganizer = participantCaptor.getValue();
            assertThat(savedOrganizer.getUloga()).isEqualTo("organizer");
            assertThat(savedOrganizer.getKorisnik()).isEqualTo(organizerUser);
            assertThat(savedOrganizer.getPutovanje().getPutovanjeId()).isEqualTo(TRIP_ID);

            verify(userRepository, times(1)).findById(ORGANIZER_USER_ID);
        }

        @Test
        @DisplayName("createTrip with end date before start date returns 400 Bad Request")
        void createTrip_withInvalidDateRange_returns400AndDoesNotPersist() throws Exception {
            // Given - an invalid date range; no repository mocks needed because
            // the real service rejects this before reaching persistence.
            CreateTripDTO invalid = CreateTripDTO.builder()
                    .naziv("Bad Trip")
                    .opis("End before start")
                    .datumPoc(LocalDate.of(2024, 6, 10))
                    .datumKraj(LocalDate.of(2024, 6, 1))
                    .build();

            // When / Then - the real service throws IllegalArgumentException, which
            // GlobalExceptionHandler maps to HTTP 400 with the descriptive message.
            mockMvc.perform(post("/api/trips")
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("End date must be after or equal to start date"));

            // Nothing should have been persisted, and the user lookup is also
            // never reached because the validation runs first.
            verifyNoInteractions(tripRepository);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("createTrip when user does not exist returns 500 with 'User not found'")
        void createTrip_whenUserDoesNotExist_returns500() throws Exception {
            // Given
            when(userRepository.findById(ORGANIZER_USER_ID))
                    .thenReturn(Optional.empty());

            CreateTripDTO request = CreateTripDTO.builder()
                    .naziv("Paris Trip")
                    .opis("Spring vacation in Paris")
                    .datumPoc(startDate)
                    .datumKraj(endDate)
                    .build();

            // When / Then - service throws RuntimeException("User not found"),
            // mapped to HTTP 500 by GlobalExceptionHandler.
            mockMvc.perform(post("/api/trips")
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("User not found"));

            // Trip and participant were never saved.
            verify(tripRepository, never()).save(any(Putovanje.class));
            verify(participantRepository, never()).save(any(Sudionik.class));
        }
    }

    // ---------------------------------------------------------------------
    // Authorization checks - non-participants cannot view trips
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Authorization checks - participant access")
    class AuthorizationChecks {

        @Test
        @DisplayName("getTripById allows a participant to view trip details")
        void getTripById_asParticipant_returnsTripDetails() throws Exception {
            // Given
            when(tripRepository.findById(TRIP_ID))
                    .thenReturn(Optional.of(persistedTrip));
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, PARTICIPANT_USER_ID))
                    .thenReturn(Optional.of(regularParticipant));
            when(participantRepository.findByPutovanje_PutovanjeId(TRIP_ID))
                    .thenReturn(Arrays.asList(organizerParticipant, regularParticipant));

            // When / Then - 200 OK with body
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Paris Trip"))
                    .andExpect(jsonPath("$.participantCount").value(2));

            // Verify the authorization lookup occurred at the service layer
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, PARTICIPANT_USER_ID);
        }

        @Test
        @DisplayName("getTripById blocks a non-participant with 'Access denied'")
        void getTripById_asNonParticipant_returns500WithAccessDenied() throws Exception {
            // Given - the trip exists, but the requesting user is not a
            // participant of it.
            when(tripRepository.findById(TRIP_ID))
                    .thenReturn(Optional.of(persistedTrip));
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            // When / Then - service throws RuntimeException("Access denied: ...")
            // which maps to 500 in the production GlobalExceptionHandler.
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            // The authorization lookup must have happened at the service layer.
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID);
        }

        @Test
        @DisplayName("getTripById returns 500 when trip does not exist")
        void getTripById_whenTripNotFound_returns500() throws Exception {
            // Given
            when(tripRepository.findById(TRIP_ID))
                    .thenReturn(Optional.empty());

            // When / Then
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            // Service short-circuits on missing trip: participant lookup not invoked.
            verify(participantRepository, never())
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(anyInt(), anyInt());
        }

        @Test
        @DisplayName("listUserTrips returns only trips where user participates")
        void listUserTrips_returnsTripsWhereUserIsParticipant() throws Exception {
            // Given
            when(tripRepository
                    .findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(PARTICIPANT_USER_ID))
                    .thenReturn(Arrays.asList(persistedTrip));
            when(participantRepository.findByPutovanje_PutovanjeId(TRIP_ID))
                    .thenReturn(Arrays.asList(organizerParticipant, regularParticipant));

            // When / Then
            mockMvc.perform(get("/api/trips")
                            .param("userId", PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$[0].participantCount").value(2));

            verify(tripRepository, times(1))
                    .findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(PARTICIPANT_USER_ID);
        }

        @Test
        @DisplayName("listUserTrips for a user with no trips returns empty list")
        void listUserTrips_forUserWithNoTrips_returnsEmptyList() throws Exception {
            // Given
            when(tripRepository
                    .findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(NON_PARTICIPANT_USER_ID))
                    .thenReturn(Collections.emptyList());

            // When / Then
            mockMvc.perform(get("/api/trips")
                            .param("userId", NON_PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ---------------------------------------------------------------------
    // Organizer-only operations - update and delete
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Organizer-only operations - update and delete")
    class OrganizerOnlyOperations {

        @Test
        @DisplayName("updateTrip succeeds when requester is an organizer")
        void updateTrip_asOrganizer_returns200WithUpdatedBody() throws Exception {
            // Given - the requester is an organizer
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));
            when(tripRepository.findById(TRIP_ID))
                    .thenReturn(Optional.of(persistedTrip));
            when(tripRepository.save(any(Putovanje.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(participantRepository.findByPutovanje_PutovanjeId(TRIP_ID))
                    .thenReturn(Arrays.asList(organizerParticipant));

            UpdateTripDTO updateRequest = UpdateTripDTO.builder()
                    .naziv("Renamed Paris Trip")
                    .opis("Updated description")
                    .build();

            // When / Then
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Renamed Paris Trip"))
                    .andExpect(jsonPath("$.opis").value("Updated description"));

            // Verify persistence happened
            ArgumentCaptor<Putovanje> tripCaptor = ArgumentCaptor.forClass(Putovanje.class);
            verify(tripRepository, times(1)).save(tripCaptor.capture());
            assertThat(tripCaptor.getValue().getNaziv()).isEqualTo("Renamed Paris Trip");
            assertThat(tripCaptor.getValue().getOpis()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("updateTrip rejects a regular participant with 'Access denied'")
        void updateTrip_asRegularParticipant_returns500AndDoesNotPersist() throws Exception {
            // Given - the requester is a participant but NOT an organizer
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, PARTICIPANT_USER_ID))
                    .thenReturn(Optional.of(regularParticipant));

            UpdateTripDTO updateRequest = UpdateTripDTO.builder()
                    .naziv("Sneaky update")
                    .build();

            // When / Then
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", PARTICIPANT_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can update trips"));

            // Authorization lookup occurred but no persistence call was made.
            verify(participantRepository, times(1))
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, PARTICIPANT_USER_ID);
            verify(tripRepository, never()).save(any(Putovanje.class));
            verify(tripRepository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("updateTrip rejects a non-participant with 'Access denied'")
        void updateTrip_asNonParticipant_returns500AndDoesNotPersist() throws Exception {
            // Given - the requester is not a participant at all (so isUserOrganizer
            // returns false based on Optional.empty()).
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            UpdateTripDTO updateRequest = UpdateTripDTO.builder()
                    .naziv("Outsider update")
                    .build();

            // When / Then
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can update trips"));

            verify(tripRepository, never()).save(any(Putovanje.class));
        }

        @Test
        @DisplayName("updateTrip with end date before start date returns 400 Bad Request")
        void updateTrip_withInvalidDateRange_returns400AndDoesNotPersist() throws Exception {
            // Given - organizer makes an invalid update
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));
            when(tripRepository.findById(TRIP_ID))
                    .thenReturn(Optional.of(persistedTrip));

            UpdateTripDTO invalid = UpdateTripDTO.builder()
                    .datumPoc(LocalDate.of(2024, 6, 20))
                    .datumKraj(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("End date must be after or equal to start date"));

            // Trip was loaded but never saved (validation rejected the update).
            verify(tripRepository, never()).save(any(Putovanje.class));
        }

        @Test
        @DisplayName("deleteTrip succeeds when requester is an organizer")
        void deleteTrip_asOrganizer_returns204AndDeletes() throws Exception {
            // Given
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));

            // When / Then
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString()))
                    .andExpect(status().isNoContent());

            verify(tripRepository, times(1)).deleteById(TRIP_ID);
        }

        @Test
        @DisplayName("deleteTrip rejects a regular participant with 'Access denied'")
        void deleteTrip_asRegularParticipant_returns500AndDoesNotDelete() throws Exception {
            // Given
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, PARTICIPANT_USER_ID))
                    .thenReturn(Optional.of(regularParticipant));

            // When / Then
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can delete trips"));

            verify(tripRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("deleteTrip rejects a non-participant with 'Access denied'")
        void deleteTrip_asNonParticipant_returns500AndDoesNotDelete() throws Exception {
            // Given
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            // When / Then
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can delete trips"));

            verify(tripRepository, never()).deleteById(anyInt());
        }
    }
}
