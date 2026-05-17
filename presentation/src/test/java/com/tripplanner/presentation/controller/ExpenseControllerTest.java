package com.tripplanner.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tripplanner.business.service.ExpenseService;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;
import com.tripplanner.presentation.base.ControllerTestBase;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ExpenseController} using MockMvc with a mocked
 * {@link ExpenseService}. Verifies HTTP status codes, JSON serialization /
 * deserialization, request validation and that the controller correctly
 * delegates to the service layer.
 *
 * <p>This test sets up MockMvc using {@link MockMvcBuilders#standaloneSetup}
 * so the test focuses on the controller and the global exception handler
 * without loading the full Spring context (or its security configuration).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseController Unit Tests")
class ExpenseControllerTest extends ControllerTestBase {

    @Mock
    private ExpenseService expenseService;

    private ExpenseController expenseController;

    private static final String BASE_URL = "/api/trips/{tripId}/expenses";
    private static final String EXPENSE_URL = "/api/trips/{tripId}/expenses/{expenseId}";

    private static final Integer TRIP_ID = 1;
    private static final Integer USER_ID = 10;
    private static final Integer EXPENSE_ID = 100;

    private ExpenseResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        expenseController = new ExpenseController(expenseService);

        // Configure ObjectMapper to handle Java 8 date types (LocalDate)
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(expenseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(jacksonConverter)
                .build();

        sampleResponse = ExpenseResponseDTO.builder()
                .trosakId(EXPENSE_ID)
                .iznos(new BigDecimal("100.50"))
                .opis("Hotel")
                .datum(LocalDate.of(2024, 6, 5))
                .putovanjeId(TRIP_ID)
                .build();
    }

    // ------------------------------------------------------------------
    //  POST /api/trips/{tripId}/expenses  (createExpense)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/trips/{tripId}/expenses")
    class CreateExpense {

        @Test
        @DisplayName("createExpense_validRequest_returns201Created")
        void createExpense_validRequest_returns201Created() throws Exception {
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("100.50"))
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            when(expenseService.createExpense(eq(TRIP_ID), eq(USER_ID), any(CreateExpenseDTO.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.trosakId").value(EXPENSE_ID))
                    .andExpect(jsonPath("$.iznos").value(100.50))
                    .andExpect(jsonPath("$.opis").value("Hotel"))
                    .andExpect(jsonPath("$.datum").value("2024-06-05"))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID));

            // Verify service interaction with correct DTO contents
            ArgumentCaptor<CreateExpenseDTO> captor = ArgumentCaptor.forClass(CreateExpenseDTO.class);
            verify(expenseService, times(1))
                    .createExpense(eq(TRIP_ID), eq(USER_ID), captor.capture());
            CreateExpenseDTO captured = captor.getValue();
            assertThat(captured.getIznos()).isEqualByComparingTo("100.50");
            assertThat(captured.getOpis()).isEqualTo("Hotel");
            assertThat(captured.getDatum()).isEqualTo(LocalDate.of(2024, 6, 5));
        }

        @Test
        @DisplayName("createExpense_missingIznos_returns400BadRequest")
        void createExpense_missingIznos_returns400BadRequest() throws Exception {
            // iznos is @NotNull
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(null)
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(expenseService);
        }

        @Test
        @DisplayName("createExpense_negativeIznos_returns400BadRequest")
        void createExpense_negativeIznos_returns400BadRequest() throws Exception {
            // iznos must be @Positive
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("-10.00"))
                    .opis("Refund")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(expenseService);
        }

        @Test
        @DisplayName("createExpense_missingDatum_returns400BadRequest")
        void createExpense_missingDatum_returns400BadRequest() throws Exception {
            // datum is @NotNull
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("50.00"))
                    .opis("Lunch")
                    .datum(null)
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(expenseService);
        }

        @Test
        @DisplayName("createExpense_missingUserIdParam_returns400BadRequest")
        void createExpense_missingUserIdParam_returns400BadRequest() throws Exception {
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("50.00"))
                    .opis("Lunch")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(expenseService);
        }

        @Test
        @DisplayName("createExpense_userNotParticipant_propagatesServiceError")
        void createExpense_userNotParticipant_propagatesServiceError() throws Exception {
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("100.00"))
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            when(expenseService.createExpense(eq(TRIP_ID), eq(USER_ID), any(CreateExpenseDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            // GlobalExceptionHandler maps generic RuntimeException -> 500
            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(expenseService, times(1))
                    .createExpense(eq(TRIP_ID), eq(USER_ID), any(CreateExpenseDTO.class));
        }

        @Test
        @DisplayName("createExpense_illegalArgument_returns400BadRequest")
        void createExpense_illegalArgument_returns400BadRequest() throws Exception {
            CreateExpenseDTO request = CreateExpenseDTO.builder()
                    .iznos(new BigDecimal("100.00"))
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .build();

            when(expenseService.createExpense(eq(TRIP_ID), eq(USER_ID), any(CreateExpenseDTO.class)))
                    .thenThrow(new IllegalArgumentException("Datum mora biti unutar perioda putovanja"));

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Datum mora biti unutar perioda putovanja"));

            verify(expenseService, times(1))
                    .createExpense(eq(TRIP_ID), eq(USER_ID), any(CreateExpenseDTO.class));
        }
    }

    // ------------------------------------------------------------------
    //  GET /api/trips/{tripId}/expenses  (listTripExpenses)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/trips/{tripId}/expenses")
    class ListTripExpenses {

        @Test
        @DisplayName("listTripExpenses_validRequest_returns200OkWithExpenseList")
        void listTripExpenses_validRequest_returns200OkWithExpenseList() throws Exception {
            ExpenseResponseDTO second = ExpenseResponseDTO.builder()
                    .trosakId(101)
                    .iznos(new BigDecimal("25.00"))
                    .opis("Taxi")
                    .datum(LocalDate.of(2024, 6, 6))
                    .putovanjeId(TRIP_ID)
                    .build();
            List<ExpenseResponseDTO> expenses = Arrays.asList(sampleResponse, second);

            when(expenseService.listTripExpenses(TRIP_ID, USER_ID)).thenReturn(expenses);

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].trosakId").value(EXPENSE_ID))
                    .andExpect(jsonPath("$[0].iznos").value(100.50))
                    .andExpect(jsonPath("$[1].trosakId").value(101))
                    .andExpect(jsonPath("$[1].opis").value("Taxi"));

            verify(expenseService, times(1)).listTripExpenses(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("listTripExpenses_noExpenses_returns200OkWithEmptyList")
        void listTripExpenses_noExpenses_returns200OkWithEmptyList() throws Exception {
            when(expenseService.listTripExpenses(TRIP_ID, USER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(expenseService, times(1)).listTripExpenses(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("listTripExpenses_userNotParticipant_propagatesServiceError")
        void listTripExpenses_userNotParticipant_propagatesServiceError() throws Exception {
            when(expenseService.listTripExpenses(TRIP_ID, USER_ID))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(expenseService, times(1)).listTripExpenses(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("listTripExpenses_missingUserId_returns400BadRequest")
        void listTripExpenses_missingUserId_returns400BadRequest() throws Exception {
            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(expenseService);
        }
    }

    // ------------------------------------------------------------------
    //  GET /api/trips/{tripId}/expenses/{expenseId}  (getExpenseById)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/trips/{tripId}/expenses/{expenseId}")
    class GetExpenseById {

        @Test
        @DisplayName("getExpenseById_validRequest_returns200OkWithExpense")
        void getExpenseById_validRequest_returns200OkWithExpense() throws Exception {
            when(expenseService.getExpenseById(EXPENSE_ID, USER_ID)).thenReturn(sampleResponse);

            mockMvc.perform(get(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trosakId").value(EXPENSE_ID))
                    .andExpect(jsonPath("$.iznos").value(100.50))
                    .andExpect(jsonPath("$.opis").value("Hotel"))
                    .andExpect(jsonPath("$.datum").value("2024-06-05"))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID));

            verify(expenseService, times(1)).getExpenseById(EXPENSE_ID, USER_ID);
        }

        @Test
        @DisplayName("getExpenseById_expenseNotFound_propagatesServiceError")
        void getExpenseById_expenseNotFound_propagatesServiceError() throws Exception {
            when(expenseService.getExpenseById(EXPENSE_ID, USER_ID))
                    .thenThrow(new RuntimeException("Expense not found"));

            mockMvc.perform(get(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Expense not found"));

            verify(expenseService, times(1)).getExpenseById(EXPENSE_ID, USER_ID);
        }
    }

    // ------------------------------------------------------------------
    //  PUT /api/trips/{tripId}/expenses/{expenseId}  (updateExpense)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/trips/{tripId}/expenses/{expenseId}")
    class UpdateExpense {

        @Test
        @DisplayName("updateExpense_validRequest_returns200OkWithUpdatedExpense")
        void updateExpense_validRequest_returns200OkWithUpdatedExpense() throws Exception {
            UpdateExpenseDTO request = UpdateExpenseDTO.builder()
                    .iznos(new BigDecimal("200.00"))
                    .opis("Updated Hotel")
                    .datum(LocalDate.of(2024, 6, 7))
                    .build();

            ExpenseResponseDTO updated = ExpenseResponseDTO.builder()
                    .trosakId(EXPENSE_ID)
                    .iznos(new BigDecimal("200.00"))
                    .opis("Updated Hotel")
                    .datum(LocalDate.of(2024, 6, 7))
                    .putovanjeId(TRIP_ID)
                    .build();

            when(expenseService.updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class)))
                    .thenReturn(updated);

            mockMvc.perform(put(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trosakId").value(EXPENSE_ID))
                    .andExpect(jsonPath("$.iznos").value(200.00))
                    .andExpect(jsonPath("$.opis").value("Updated Hotel"))
                    .andExpect(jsonPath("$.datum").value("2024-06-07"));

            ArgumentCaptor<UpdateExpenseDTO> captor = ArgumentCaptor.forClass(UpdateExpenseDTO.class);
            verify(expenseService, times(1))
                    .updateExpense(eq(EXPENSE_ID), eq(USER_ID), captor.capture());
            UpdateExpenseDTO captured = captor.getValue();
            assertThat(captured.getIznos()).isEqualByComparingTo("200.00");
            assertThat(captured.getOpis()).isEqualTo("Updated Hotel");
            assertThat(captured.getDatum()).isEqualTo(LocalDate.of(2024, 6, 7));
        }

        @Test
        @DisplayName("updateExpense_partialUpdate_returns200OkWithMergedFields")
        void updateExpense_partialUpdate_returns200OkWithMergedFields() throws Exception {
            // Only iznos provided - other fields remain null in the DTO and
            // the service is responsible for retaining current values.
            UpdateExpenseDTO request = UpdateExpenseDTO.builder()
                    .iznos(new BigDecimal("75.25"))
                    .build();

            ExpenseResponseDTO updated = ExpenseResponseDTO.builder()
                    .trosakId(EXPENSE_ID)
                    .iznos(new BigDecimal("75.25"))
                    .opis("Hotel")
                    .datum(LocalDate.of(2024, 6, 5))
                    .putovanjeId(TRIP_ID)
                    .build();

            when(expenseService.updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class)))
                    .thenReturn(updated);

            mockMvc.perform(put(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.iznos").value(75.25))
                    .andExpect(jsonPath("$.opis").value("Hotel"));

            verify(expenseService, times(1))
                    .updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class));
        }

        @Test
        @DisplayName("updateExpense_userNotParticipant_propagatesServiceError")
        void updateExpense_userNotParticipant_propagatesServiceError() throws Exception {
            UpdateExpenseDTO request = UpdateExpenseDTO.builder()
                    .iznos(new BigDecimal("200.00"))
                    .build();

            when(expenseService.updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            mockMvc.perform(put(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(expenseService, times(1))
                    .updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class));
        }

        @Test
        @DisplayName("updateExpense_expenseNotFound_propagatesServiceError")
        void updateExpense_expenseNotFound_propagatesServiceError() throws Exception {
            UpdateExpenseDTO request = UpdateExpenseDTO.builder()
                    .iznos(new BigDecimal("200.00"))
                    .build();

            when(expenseService.updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class)))
                    .thenThrow(new RuntimeException("Expense not found"));

            mockMvc.perform(put(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Expense not found"));

            verify(expenseService, times(1))
                    .updateExpense(eq(EXPENSE_ID), eq(USER_ID), any(UpdateExpenseDTO.class));
        }
    }

    // ------------------------------------------------------------------
    //  DELETE /api/trips/{tripId}/expenses/{expenseId}  (deleteExpense)
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/trips/{tripId}/expenses/{expenseId}")
    class DeleteExpense {

        @Test
        @DisplayName("deleteExpense_validRequest_returns204NoContent")
        void deleteExpense_validRequest_returns204NoContent() throws Exception {
            doNothing().when(expenseService).deleteExpense(EXPENSE_ID, USER_ID);

            mockMvc.perform(delete(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(expenseService, times(1)).deleteExpense(EXPENSE_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteExpense_userNotParticipant_propagatesServiceError")
        void deleteExpense_userNotParticipant_propagatesServiceError() throws Exception {
            doThrow(new RuntimeException("Access denied: User is not a participant of this trip"))
                    .when(expenseService).deleteExpense(EXPENSE_ID, USER_ID);

            mockMvc.perform(delete(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(expenseService, times(1)).deleteExpense(EXPENSE_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteExpense_expenseNotFound_propagatesServiceError")
        void deleteExpense_expenseNotFound_propagatesServiceError() throws Exception {
            doThrow(new RuntimeException("Expense not found"))
                    .when(expenseService).deleteExpense(EXPENSE_ID, USER_ID);

            mockMvc.perform(delete(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Expense not found"));

            verify(expenseService, times(1)).deleteExpense(EXPENSE_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteExpense_missingUserIdParam_returns400BadRequest")
        void deleteExpense_missingUserIdParam_returns400BadRequest() throws Exception {
            mockMvc.perform(delete(EXPENSE_URL, TRIP_ID, EXPENSE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(expenseService, never()).deleteExpense(any(), any());
        }
    }
}
