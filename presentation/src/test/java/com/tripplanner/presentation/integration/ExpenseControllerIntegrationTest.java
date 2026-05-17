package com.tripplanner.presentation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripplanner.business.service.impl.ExpenseServiceImpl;
import com.tripplanner.business.service.impl.TripServiceImpl;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import com.tripplanner.domain.entity.Trosak;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.controller.ExpenseController;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ExpenseController} with the real
 * {@link ExpenseServiceImpl} and {@link TripServiceImpl} wired in,
 * and the underlying repositories mocked.
 *
 * <p>This verifies the request-response flow through the presentation
 * layer into the business layer without touching a real database. The
 * focus of task 10.2 is on the expense creation endpoint, in particular:
 * <ul>
 *   <li>Successful flow returns 201 and persists an expense + recalculates the trip total</li>
 *   <li>Authorization checks prevent access by non-participants</li>
 *   <li>Error responses surface descriptive messages</li>
 *   <li>Request validation rejects invalid bodies before reaching the service</li>
 * </ul>
 *
 * <p>Validates: Requirements 5.1, 5.3, 5.5, 5.7, 5.9</p>
 */
@WebMvcTest(
        controllers = ExpenseController.class,
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
@Import({
        ExpenseServiceImpl.class,
        TripServiceImpl.class,
        GlobalExceptionHandler.class
})
@DisplayName("ExpenseController <-> ExpenseService Integration Tests")
@Tag("integration")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repository layer is mocked - service implementations are real
    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private TripRepository tripRepository;

    @MockBean
    private ParticipantRepository participantRepository;

    @MockBean
    private UserRepository userRepository;

    private static final Integer TRIP_ID = 1;
    private static final Integer ORGANIZER_USER_ID = 10;
    private static final Integer NON_PARTICIPANT_USER_ID = 99;
    private static final Integer EXPENSE_ID = 100;
    private static final String BASE_URL = "/api/trips/{tripId}/expenses";

    private Putovanje trip;
    private Korisnik organizer;
    private Sudionik organizerParticipant;

    @BeforeEach
    void setUp() {
        organizer = Korisnik.builder()
                .korisnikId(ORGANIZER_USER_ID)
                .ime("Alice")
                .prezime("Anderson")
                .email("alice@example.com")
                .build();

        trip = Putovanje.builder()
                .putovanjeId(TRIP_ID)
                .naziv("Paris Trip")
                .opis("Spring vacation in Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.ZERO)
                .build();

        organizerParticipant = Sudionik.builder()
                .sudionikId(500)
                .korisnik(organizer)
                .putovanje(trip)
                .uloga("organizer")
                .build();
    }

    // ------------------------------------------------------------------
    //  POST /api/trips/{tripId}/expenses  (createExpense)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/trips/{tripId}/expenses (createExpense)")
    class CreateExpense {

        @Test
        @DisplayName("createExpense_validRequestAsParticipant_returns201AndRecalculatesTripTotal")
        void createExpense_validRequestAsParticipant_returns201AndRecalculatesTripTotal() throws Exception {
            // Given: user is a participant of the trip
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));

            // Trip exists
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));

            // Saving the expense returns a persisted instance with an ID
            when(expenseRepository.save(any(Trosak.class))).thenAnswer(invocation -> {
                Trosak saved = invocation.getArgument(0);
                saved.setTrosakId(EXPENSE_ID);
                return saved;
            });

            // After saving, the recalculation queries the new total
            when(expenseRepository.sumByPutovanjeId(TRIP_ID)).thenReturn(new BigDecimal("100.50"));

            // The participant count map needs at least one participant for the response DTO
            when(participantRepository.findByPutovanje_PutovanjeId(TRIP_ID))
                    .thenReturn(List.of(organizerParticipant));

            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("100.50"))
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.trosakId").value(EXPENSE_ID))
                    .andExpect(jsonPath("$.iznos").value(100.50))
                    .andExpect(jsonPath("$.opis").value("Hotel"))
                    .andExpect(jsonPath("$.datum").value("2024-06-05"))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID));

            // Verify the expense was persisted with the correct values
            verify(expenseRepository, times(1)).save(any(Trosak.class));

            // Verify the trip total was recalculated and persisted
            verify(expenseRepository, atLeastOnce()).sumByPutovanjeId(TRIP_ID);
            verify(tripRepository, atLeastOnce()).save(any(Putovanje.class));
            assertThat(trip.getUkTrosak()).isEqualByComparingTo("100.50");
        }

        @Test
        @DisplayName("createExpense_userNotParticipant_returns500WithAccessDeniedMessage")
        void createExpense_userNotParticipant_returns500WithAccessDeniedMessage() throws Exception {
            // Given: user is NOT a participant of the trip
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, NON_PARTICIPANT_USER_ID))
                    .thenReturn(Optional.empty());

            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("75.00"))
                    .opis("Lunch")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then - the real service throws RuntimeException("Access denied: ..."),
            // which the GlobalExceptionHandler maps to HTTP 500 with the error message in
            // the response body. This verifies authorization checks reach all the way
            // through the controller and that the error response carries the message.
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", NON_PARTICIPANT_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.timestamp").exists());

            // Critically, no expense should have been persisted and no recalculation
            // should have happened when the authorization check fails.
            verify(expenseRepository, never()).save(any(Trosak.class));
            verify(expenseRepository, never()).sumByPutovanjeId(any());
            verify(tripRepository, never()).save(any(Putovanje.class));
        }

        @Test
        @DisplayName("createExpense_tripNotFound_returns500WithTripNotFoundMessage")
        void createExpense_tripNotFound_returns500WithTripNotFoundMessage() throws Exception {
            // Given: user is a participant but the trip itself cannot be loaded
            when(participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(TRIP_ID, ORGANIZER_USER_ID))
                    .thenReturn(Optional.of(organizerParticipant));
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("75.00"))
                    .opis("Lunch")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(expenseRepository, never()).save(any(Trosak.class));
        }

        @Test
        @DisplayName("createExpense_missingIznos_returns400AndDoesNotInvokeService")
        void createExpense_missingIznos_returns400AndDoesNotInvokeService() throws Exception {
            // Given - iznos is @NotNull on CreateExpenseDTO
            CreateExpenseDTO invalid = CreateExpenseDTO.builder()
                    .iznos(null)
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then - validation rejects request before service is hit
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(expenseRepository, never()).save(any(Trosak.class));
            verify(participantRepository, never())
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(any(), any());
        }

        @Test
        @DisplayName("createExpense_negativeIznos_returns400AndDoesNotInvokeService")
        void createExpense_negativeIznos_returns400AndDoesNotInvokeService() throws Exception {
            // Given - iznos is @Positive on CreateExpenseDTO
            CreateExpenseDTO invalid = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("-10.00"))
                    .opis("Refund")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(expenseRepository, never()).save(any(Trosak.class));
        }

        @Test
        @DisplayName("createExpense_missingDatum_returns400AndDoesNotInvokeService")
        void createExpense_missingDatum_returns400AndDoesNotInvokeService() throws Exception {
            // Given - datum is @NotNull on CreateExpenseDTO
            CreateExpenseDTO invalid = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("50.00"))
                    .opis("Lunch")
                    .datum(null)
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(expenseRepository, never()).save(any(Trosak.class));
        }
    }
}
