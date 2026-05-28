package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.TripService;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.presentation.base.ControllerTestBase;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
@Import(GlobalExceptionHandler.class)
@DisplayName("TripController Tests")
class TripControllerTest extends ControllerTestBase {

    @MockBean
    private TripService tripService;

    private static final Integer TRIP_ID = 1;
    private static final Integer USER_ID = 10;
    private static final Integer OTHER_USER_ID = 99;

    private LocalDate startDate;
    private LocalDate endDate;
    private CreateTripDTO validCreateDTO;
    private UpdateTripDTO validUpdateDTO;
    private TripResponseDTO tripResponse;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 6, 1);
        endDate = LocalDate.of(2024, 6, 10);

        validCreateDTO = CreateTripDTO.builder()
                .naziv("Paris Trip")
                .opis("Spring vacation in Paris")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .build();

        validUpdateDTO = UpdateTripDTO.builder()
                .naziv("Updated Paris Trip")
                .opis("Updated description")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .build();

        tripResponse = TripResponseDTO.builder()
                .putovanjeId(TRIP_ID)
                .naziv("Paris Trip")
                .opis("Spring vacation in Paris")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .ukTrosak(BigDecimal.ZERO)
                .participantCount(1)
                .build();
    }

    
    
    

    @Nested
    @DisplayName("POST /api/trips")
    class CreateTripEndpoint {

        @Test
        @DisplayName("createTrip_withValidRequest_returns201CreatedWithBody")
        void createTrip_withValidRequest_returns201CreatedWithBody() throws Exception {
            
            when(tripService.createTrip(eq(USER_ID), any(CreateTripDTO.class)))
                    .thenReturn(tripResponse);

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Paris Trip"))
                    .andExpect(jsonPath("$.opis").value("Spring vacation in Paris"))
                    .andExpect(jsonPath("$.datumPoc").value("2024-06-01"))
                    .andExpect(jsonPath("$.datumKraj").value("2024-06-10"))
                    .andExpect(jsonPath("$.ukTrosak").value(0))
                    .andExpect(jsonPath("$.participantCount").value(1));

            verify(tripService, times(1))
                    .createTrip(eq(USER_ID), any(CreateTripDTO.class));
        }

        @Test
        @DisplayName("createTrip_deserializesRequestBodyToDTO")
        void createTrip_deserializesRequestBodyToDTO() throws Exception {
            
            when(tripService.createTrip(anyInt(), any(CreateTripDTO.class)))
                    .thenReturn(tripResponse);

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isCreated());

            
            ArgumentCaptor<CreateTripDTO> captor = ArgumentCaptor.forClass(CreateTripDTO.class);
            verify(tripService).createTrip(eq(USER_ID), captor.capture());

            CreateTripDTO captured = captor.getValue();
            assertThat(captured.getNaziv()).isEqualTo("Paris Trip");
            assertThat(captured.getOpis()).isEqualTo("Spring vacation in Paris");
            assertThat(captured.getDatumPoc()).isEqualTo(startDate);
            assertThat(captured.getDatumKraj()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("createTrip_withMissingNaziv_returns400BadRequest")
        void createTrip_withMissingNaziv_returns400BadRequest() throws Exception {
            
            CreateTripDTO invalid = CreateTripDTO.builder()
                    .naziv("")
                    .datumPoc(startDate)
                    .datumKraj(endDate)
                    .build();

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("createTrip_withMissingDatumPoc_returns400BadRequest")
        void createTrip_withMissingDatumPoc_returns400BadRequest() throws Exception {
            
            CreateTripDTO invalid = CreateTripDTO.builder()
                    .naziv("Valid Trip")
                    .datumPoc(null)
                    .datumKraj(endDate)
                    .build();

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("createTrip_withMissingDatumKraj_returns400BadRequest")
        void createTrip_withMissingDatumKraj_returns400BadRequest() throws Exception {
            
            CreateTripDTO invalid = CreateTripDTO.builder()
                    .naziv("Valid Trip")
                    .datumPoc(startDate)
                    .datumKraj(null)
                    .build();

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("createTrip_withMissingUserIdParam_returns400BadRequest")
        void createTrip_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("createTrip_withMalformedJson_returnsErrorAndDoesNotCallService")
        void createTrip_withMalformedJson_returnsErrorAndDoesNotCallService() throws Exception {
            
            
            
            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ this is not valid json"))
                    .andExpect(status().is5xxServerError());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("createTrip_whenEndDateBeforeStartDate_propagates400BadRequest")
        void createTrip_whenEndDateBeforeStartDate_propagates400BadRequest() throws Exception {
            
            when(tripService.createTrip(eq(USER_ID), any(CreateTripDTO.class)))
                    .thenThrow(new IllegalArgumentException(
                            "End date must be after or equal to start date"));

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("End date must be after or equal to start date"));

            verify(tripService, times(1))
                    .createTrip(eq(USER_ID), any(CreateTripDTO.class));
        }

        @Test
        @DisplayName("createTrip_whenUserNotFound_propagatesError")
        void createTrip_whenUserNotFound_propagatesError() throws Exception {
            
            when(tripService.createTrip(eq(USER_ID), any(CreateTripDTO.class)))
                    .thenThrow(new RuntimeException("User not found"));

            
            mockMvc.perform(post("/api/trips")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("User not found"));

            verify(tripService, times(1))
                    .createTrip(eq(USER_ID), any(CreateTripDTO.class));
        }
    }

    
    
    

    @Nested
    @DisplayName("GET /api/trips/{tripId}")
    class GetTripByIdEndpoint {

        @Test
        @DisplayName("getTripById_withValidRequest_returns200OkWithBody")
        void getTripById_withValidRequest_returns200OkWithBody() throws Exception {
            
            when(tripService.getTripById(TRIP_ID, USER_ID)).thenReturn(tripResponse);

            
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Paris Trip"))
                    .andExpect(jsonPath("$.opis").value("Spring vacation in Paris"))
                    .andExpect(jsonPath("$.datumPoc").value("2024-06-01"))
                    .andExpect(jsonPath("$.datumKraj").value("2024-06-10"))
                    .andExpect(jsonPath("$.participantCount").value(1));

            verify(tripService, times(1)).getTripById(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("getTripById_withMissingUserIdParam_returns400BadRequest")
        void getTripById_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("getTripById_whenUserNotParticipant_propagatesAccessDenied")
        void getTripById_whenUserNotParticipant_propagatesAccessDenied() throws Exception {
            
            when(tripService.getTripById(TRIP_ID, OTHER_USER_ID))
                    .thenThrow(new RuntimeException(
                            "Access denied: User is not a participant of this trip"));

            
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", OTHER_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(tripService, times(1)).getTripById(TRIP_ID, OTHER_USER_ID);
        }

        @Test
        @DisplayName("getTripById_whenTripNotFound_propagatesError")
        void getTripById_whenTripNotFound_propagatesError() throws Exception {
            
            when(tripService.getTripById(TRIP_ID, USER_ID))
                    .thenThrow(new RuntimeException("Trip not found"));

            
            mockMvc.perform(get("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            verify(tripService, times(1)).getTripById(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("getTripById_withNonNumericTripId_doesNotCallService")
        void getTripById_withNonNumericTripId_doesNotCallService() throws Exception {
            
            
            
            
            mockMvc.perform(get("/api/trips/{tripId}", "not-a-number")
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().is5xxServerError());

            verifyNoInteractions(tripService);
        }
    }

    
    
    

    @Nested
    @DisplayName("GET /api/trips")
    class ListUserTripsEndpoint {

        @Test
        @DisplayName("listUserTrips_withValidRequest_returns200OkWithList")
        void listUserTrips_withValidRequest_returns200OkWithList() throws Exception {
            
            TripResponseDTO second = TripResponseDTO.builder()
                    .putovanjeId(TRIP_ID + 1)
                    .naziv("Rome Trip")
                    .opis("Roman holiday")
                    .datumPoc(startDate.plusMonths(1))
                    .datumKraj(endDate.plusMonths(1))
                    .ukTrosak(new BigDecimal("250.00"))
                    .participantCount(3)
                    .build();

            when(tripService.listUserTrips(USER_ID))
                    .thenReturn(Arrays.asList(tripResponse, second));

            
            mockMvc.perform(get("/api/trips")
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$[0].naziv").value("Paris Trip"))
                    .andExpect(jsonPath("$[1].putovanjeId").value(TRIP_ID + 1))
                    .andExpect(jsonPath("$[1].naziv").value("Rome Trip"))
                    .andExpect(jsonPath("$[1].participantCount").value(3));

            verify(tripService, times(1)).listUserTrips(USER_ID);
        }

        @Test
        @DisplayName("listUserTrips_whenUserHasNoTrips_returns200OkWithEmptyList")
        void listUserTrips_whenUserHasNoTrips_returns200OkWithEmptyList() throws Exception {
            
            when(tripService.listUserTrips(USER_ID)).thenReturn(Collections.emptyList());

            
            mockMvc.perform(get("/api/trips")
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(tripService, times(1)).listUserTrips(USER_ID);
        }

        @Test
        @DisplayName("listUserTrips_withMissingUserIdParam_returns400BadRequest")
        void listUserTrips_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(get("/api/trips"))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(tripService);
        }
    }

    
    
    

    @Nested
    @DisplayName("PUT /api/trips/{tripId}")
    class UpdateTripEndpoint {

        @Test
        @DisplayName("updateTrip_withValidRequest_returns200OkWithUpdatedBody")
        void updateTrip_withValidRequest_returns200OkWithUpdatedBody() throws Exception {
            
            TripResponseDTO updated = TripResponseDTO.builder()
                    .putovanjeId(TRIP_ID)
                    .naziv("Updated Paris Trip")
                    .opis("Updated description")
                    .datumPoc(startDate)
                    .datumKraj(endDate)
                    .ukTrosak(BigDecimal.ZERO)
                    .participantCount(1)
                    .build();

            when(tripService.updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class)))
                    .thenReturn(updated);

            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.putovanjeId").value(TRIP_ID))
                    .andExpect(jsonPath("$.naziv").value("Updated Paris Trip"))
                    .andExpect(jsonPath("$.opis").value("Updated description"));

            verify(tripService, times(1))
                    .updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class));
        }

        @Test
        @DisplayName("updateTrip_withPartialBody_returns200Ok")
        void updateTrip_withPartialBody_returns200Ok() throws Exception {
            
            UpdateTripDTO partial = UpdateTripDTO.builder()
                    .naziv("Renamed Trip")
                    .build();

            when(tripService.updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class)))
                    .thenReturn(tripResponse);

            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partial)))
                    .andExpect(status().isOk());

            
            ArgumentCaptor<UpdateTripDTO> captor = ArgumentCaptor.forClass(UpdateTripDTO.class);
            verify(tripService, times(1))
                    .updateTrip(eq(TRIP_ID), eq(USER_ID), captor.capture());
            assertThat(captor.getValue().getNaziv()).isEqualTo("Renamed Trip");
            assertThat(captor.getValue().getOpis()).isNull();
            assertThat(captor.getValue().getDatumPoc()).isNull();
            assertThat(captor.getValue().getDatumKraj()).isNull();
        }

        @Test
        @DisplayName("updateTrip_withMissingUserIdParam_returns400BadRequest")
        void updateTrip_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("updateTrip_whenUserNotOrganizer_propagatesAccessDenied")
        void updateTrip_whenUserNotOrganizer_propagatesAccessDenied() throws Exception {
            
            when(tripService.updateTrip(eq(TRIP_ID), eq(OTHER_USER_ID), any(UpdateTripDTO.class)))
                    .thenThrow(new RuntimeException(
                            "Access denied: Only organizers can update trips"));

            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", OTHER_USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can update trips"));

            verify(tripService, times(1))
                    .updateTrip(eq(TRIP_ID), eq(OTHER_USER_ID), any(UpdateTripDTO.class));
        }

        @Test
        @DisplayName("updateTrip_whenEndDateBeforeStartDate_propagates400BadRequest")
        void updateTrip_whenEndDateBeforeStartDate_propagates400BadRequest() throws Exception {
            
            when(tripService.updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class)))
                    .thenThrow(new IllegalArgumentException(
                            "End date must be after or equal to start date"));

            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("End date must be after or equal to start date"));

            verify(tripService, times(1))
                    .updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class));
        }

        @Test
        @DisplayName("updateTrip_whenTripNotFound_propagatesError")
        void updateTrip_whenTripNotFound_propagatesError() throws Exception {
            
            when(tripService.updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class)))
                    .thenThrow(new RuntimeException("Trip not found"));

            
            mockMvc.perform(put("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            verify(tripService, times(1))
                    .updateTrip(eq(TRIP_ID), eq(USER_ID), any(UpdateTripDTO.class));
        }
    }

    
    
    

    @Nested
    @DisplayName("DELETE /api/trips/{tripId}")
    class DeleteTripEndpoint {

        @Test
        @DisplayName("deleteTrip_withValidRequest_returns204NoContent")
        void deleteTrip_withValidRequest_returns204NoContent() throws Exception {
            
            doNothing().when(tripService).deleteTrip(TRIP_ID, USER_ID);

            
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(tripService, times(1)).deleteTrip(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteTrip_withMissingUserIdParam_returns400BadRequest")
        void deleteTrip_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(tripService);
        }

        @Test
        @DisplayName("deleteTrip_whenUserNotOrganizer_propagatesAccessDenied")
        void deleteTrip_whenUserNotOrganizer_propagatesAccessDenied() throws Exception {
            
            doThrow(new RuntimeException("Access denied: Only organizers can delete trips"))
                    .when(tripService).deleteTrip(TRIP_ID, OTHER_USER_ID);

            
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", OTHER_USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can delete trips"));

            verify(tripService, times(1)).deleteTrip(TRIP_ID, OTHER_USER_ID);
        }

        @Test
        @DisplayName("deleteTrip_whenTripNotFound_propagatesError")
        void deleteTrip_whenTripNotFound_propagatesError() throws Exception {
            
            doThrow(new RuntimeException("Trip not found"))
                    .when(tripService).deleteTrip(TRIP_ID, USER_ID);

            
            mockMvc.perform(delete("/api/trips/{tripId}", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            verify(tripService, times(1)).deleteTrip(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteTrip_withNonNumericTripId_doesNotCallService")
        void deleteTrip_withNonNumericTripId_doesNotCallService() throws Exception {
            
            
            
            
            mockMvc.perform(delete("/api/trips/{tripId}", "abc")
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().is5xxServerError());

            verify(tripService, never()).deleteTrip(anyInt(), anyInt());
        }
    }
}
