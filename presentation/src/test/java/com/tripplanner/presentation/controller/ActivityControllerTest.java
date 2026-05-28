package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.ActivityService;
import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CategoryResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;
import com.tripplanner.presentation.base.ControllerTestBase;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.hasSize;
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
@Import(GlobalExceptionHandler.class)
@DisplayName("ActivityController Tests")
class ActivityControllerTest extends ControllerTestBase {

    @MockBean
    private ActivityService activityService;

    private static final Integer TRIP_ID = 10;
    private static final Integer ACTIVITY_ID = 100;
    private static final Integer USER_ID = 1;
    private static final Integer LOCATION_ID = 50;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private CreateActivityDTO validCreateDTO;
    private UpdateActivityDTO validUpdateDTO;
    private ActivityResponseDTO activityResponse;
    private LocationResponseDTO locationResponse;
    private CategoryResponseDTO categoryResponse;

    @BeforeEach
    void setUp() {
        startDateTime = LocalDateTime.of(2024, 6, 1, 10, 0);
        endDateTime = LocalDateTime.of(2024, 6, 1, 12, 0);

        validCreateDTO = CreateActivityDTO.builder()
                .naziv("Eiffel Tower Visit")
                .opis("Visit the iconic Eiffel Tower")
                .datumVrijemePoc(startDateTime)
                .datumVrijemeKraj(endDateTime)
                .lokacijaId(LOCATION_ID)
                .categoryIds(Arrays.asList(1, 2))
                .build();

        validUpdateDTO = UpdateActivityDTO.builder()
                .naziv("Updated Eiffel Tower Visit")
                .opis("Updated description")
                .datumVrijemePoc(startDateTime)
                .datumVrijemeKraj(endDateTime)
                .lokacijaId(LOCATION_ID)
                .categoryIds(Arrays.asList(1, 2))
                .build();

        locationResponse = LocationResponseDTO.builder()
                .lokacijaId(LOCATION_ID)
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars, 5 Av. Anatole France")
                .grad("Paris")
                .drzava("France")
                .build();

        categoryResponse = CategoryResponseDTO.builder()
                .kategorijaId(1)
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build();

        activityResponse = ActivityResponseDTO.builder()
                .aktivnostId(ACTIVITY_ID)
                .naziv("Eiffel Tower Visit")
                .opis("Visit the iconic Eiffel Tower")
                .datumVrijemePoc(startDateTime)
                .datumVrijemeKraj(endDateTime)
                .location(locationResponse)
                .categories(Collections.singletonList(categoryResponse))
                .build();
    }

    
    
    

    @Nested
    @DisplayName("POST /api/trips/{tripId}/activities")
    class CreateActivityEndpoint {

        @Test
        @DisplayName("createActivity_withValidRequest_returns201CreatedWithBody")
        void createActivity_withValidRequest_returns201CreatedWithBody() throws Exception {
            
            when(activityService.createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class)))
                    .thenReturn(activityResponse);

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.aktivnostId").value(ACTIVITY_ID))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower Visit"))
                    .andExpect(jsonPath("$.opis").value("Visit the iconic Eiffel Tower"))
                    .andExpect(jsonPath("$.location.lokacijaId").value(LOCATION_ID))
                    .andExpect(jsonPath("$.location.naziv").value("Eiffel Tower"))
                    .andExpect(jsonPath("$.categories", hasSize(1)))
                    .andExpect(jsonPath("$.categories[0].kategorijaId").value(1));

            verify(activityService, times(1))
                    .createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class));
        }

        @Test
        @DisplayName("createActivity_withMissingNaziv_returns400BadRequest")
        void createActivity_withMissingNaziv_returns400BadRequest() throws Exception {
            
            CreateActivityDTO invalid = CreateActivityDTO.builder()
                    .naziv("")
                    .datumVrijemePoc(startDateTime)
                    .datumVrijemeKraj(endDateTime)
                    .lokacijaId(LOCATION_ID)
                    .build();

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("createActivity_withMissingStartDateTime_returns400BadRequest")
        void createActivity_withMissingStartDateTime_returns400BadRequest() throws Exception {
            
            CreateActivityDTO invalid = CreateActivityDTO.builder()
                    .naziv("Valid Name")
                    .datumVrijemePoc(null)
                    .datumVrijemeKraj(endDateTime)
                    .lokacijaId(LOCATION_ID)
                    .build();

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("createActivity_withMissingLocationId_returns400BadRequest")
        void createActivity_withMissingLocationId_returns400BadRequest() throws Exception {
            
            CreateActivityDTO invalid = CreateActivityDTO.builder()
                    .naziv("Valid Name")
                    .datumVrijemePoc(startDateTime)
                    .datumVrijemeKraj(endDateTime)
                    .lokacijaId(null)
                    .build();

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("createActivity_withMissingUserIdParam_returns400BadRequest")
        void createActivity_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("createActivity_withMalformedJson_returnsErrorAndDoesNotCallService")
        void createActivity_withMalformedJson_returnsErrorAndDoesNotCallService() throws Exception {
            
            
            
            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ this is not valid json"))
                    .andExpect(status().is5xxServerError());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("createActivity_whenEndBeforeStart_propagates400BadRequest")
        void createActivity_whenEndBeforeStart_propagates400BadRequest() throws Exception {
            
            when(activityService.createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class)))
                    .thenThrow(new IllegalArgumentException("End datetime must be after start datetime"));

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("End datetime must be after start datetime"));

            verify(activityService, times(1))
                    .createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class));
        }

        @Test
        @DisplayName("createActivity_whenUserNotParticipant_propagatesAccessDenied")
        void createActivity_whenUserNotParticipant_propagatesAccessDenied() throws Exception {
            
            when(activityService.createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(activityService, times(1))
                    .createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class));
        }

        @Test
        @DisplayName("createActivity_whenTripNotFound_propagatesError")
        void createActivity_whenTripNotFound_propagatesError() throws Exception {
            
            when(activityService.createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class)))
                    .thenThrow(new RuntimeException("Trip not found"));

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Trip not found"));

            verify(activityService, times(1))
                    .createActivity(eq(TRIP_ID), eq(USER_ID), any(CreateActivityDTO.class));
        }

        @Test
        @DisplayName("createActivity_deserializesRequestBodyToDTO")
        void createActivity_deserializesRequestBodyToDTO() throws Exception {
            
            when(activityService.createActivity(anyInt(), anyInt(), any(CreateActivityDTO.class)))
                    .thenReturn(activityResponse);

            
            mockMvc.perform(post("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateDTO)))
                    .andExpect(status().isCreated());

            
            org.mockito.ArgumentCaptor<CreateActivityDTO> captor =
                    org.mockito.ArgumentCaptor.forClass(CreateActivityDTO.class);
            verify(activityService).createActivity(eq(TRIP_ID), eq(USER_ID), captor.capture());

            CreateActivityDTO captured = captor.getValue();
            org.assertj.core.api.Assertions.assertThat(captured.getNaziv())
                    .isEqualTo("Eiffel Tower Visit");
            org.assertj.core.api.Assertions.assertThat(captured.getDatumVrijemePoc())
                    .isEqualTo(startDateTime);
            org.assertj.core.api.Assertions.assertThat(captured.getDatumVrijemeKraj())
                    .isEqualTo(endDateTime);
            org.assertj.core.api.Assertions.assertThat(captured.getLokacijaId())
                    .isEqualTo(LOCATION_ID);
            org.assertj.core.api.Assertions.assertThat(captured.getCategoryIds())
                    .containsExactly(1, 2);
        }
    }

    
    
    

    @Nested
    @DisplayName("GET /api/trips/{tripId}/activities")
    class ListTripActivitiesEndpoint {

        @Test
        @DisplayName("listTripActivities_withValidRequest_returns200OkWithList")
        void listTripActivities_withValidRequest_returns200OkWithList() throws Exception {
            
            ActivityResponseDTO second = ActivityResponseDTO.builder()
                    .aktivnostId(ACTIVITY_ID + 1)
                    .naziv("Louvre Museum")
                    .opis("Art museum visit")
                    .datumVrijemePoc(startDateTime.plusDays(1))
                    .datumVrijemeKraj(endDateTime.plusDays(1))
                    .location(locationResponse)
                    .categories(Collections.emptyList())
                    .build();

            when(activityService.listTripActivities(TRIP_ID, USER_ID))
                    .thenReturn(Arrays.asList(activityResponse, second));

            
            mockMvc.perform(get("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].aktivnostId").value(ACTIVITY_ID))
                    .andExpect(jsonPath("$[0].naziv").value("Eiffel Tower Visit"))
                    .andExpect(jsonPath("$[1].aktivnostId").value(ACTIVITY_ID + 1))
                    .andExpect(jsonPath("$[1].naziv").value("Louvre Museum"));

            verify(activityService, times(1)).listTripActivities(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("listTripActivities_whenNoActivities_returns200OkWithEmptyList")
        void listTripActivities_whenNoActivities_returns200OkWithEmptyList() throws Exception {
            
            when(activityService.listTripActivities(TRIP_ID, USER_ID))
                    .thenReturn(Collections.emptyList());

            
            mockMvc.perform(get("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(activityService, times(1)).listTripActivities(TRIP_ID, USER_ID);
        }

        @Test
        @DisplayName("listTripActivities_withMissingUserIdParam_returns400BadRequest")
        void listTripActivities_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(get("/api/trips/{tripId}/activities", TRIP_ID))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("listTripActivities_whenUserNotParticipant_propagatesAccessDenied")
        void listTripActivities_whenUserNotParticipant_propagatesAccessDenied() throws Exception {
            
            when(activityService.listTripActivities(TRIP_ID, USER_ID))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            
            mockMvc.perform(get("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(activityService, times(1)).listTripActivities(TRIP_ID, USER_ID);
        }
    }

    
    
    

    @Nested
    @DisplayName("GET /api/trips/{tripId}/activities/{activityId}")
    class GetActivityByIdEndpoint {

        @Test
        @DisplayName("getActivityById_withValidRequest_returns200OkWithBody")
        void getActivityById_withValidRequest_returns200OkWithBody() throws Exception {
            
            when(activityService.getActivityById(ACTIVITY_ID, USER_ID))
                    .thenReturn(activityResponse);

            
            mockMvc.perform(get("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.aktivnostId").value(ACTIVITY_ID))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower Visit"))
                    .andExpect(jsonPath("$.location.lokacijaId").value(LOCATION_ID));

            verify(activityService, times(1)).getActivityById(ACTIVITY_ID, USER_ID);
        }

        @Test
        @DisplayName("getActivityById_whenActivityNotFound_propagatesError")
        void getActivityById_whenActivityNotFound_propagatesError() throws Exception {
            
            when(activityService.getActivityById(ACTIVITY_ID, USER_ID))
                    .thenThrow(new RuntimeException("Activity not found"));

            
            mockMvc.perform(get("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Activity not found"));

            verify(activityService, times(1)).getActivityById(ACTIVITY_ID, USER_ID);
        }
    }

    
    
    

    @Nested
    @DisplayName("PUT /api/trips/{tripId}/activities/{activityId}")
    class UpdateActivityEndpoint {

        @Test
        @DisplayName("updateActivity_withValidRequest_returns200OkWithUpdatedBody")
        void updateActivity_withValidRequest_returns200OkWithUpdatedBody() throws Exception {
            
            ActivityResponseDTO updated = ActivityResponseDTO.builder()
                    .aktivnostId(ACTIVITY_ID)
                    .naziv("Updated Eiffel Tower Visit")
                    .opis("Updated description")
                    .datumVrijemePoc(startDateTime)
                    .datumVrijemeKraj(endDateTime)
                    .location(locationResponse)
                    .categories(Collections.singletonList(categoryResponse))
                    .build();

            when(activityService.updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class)))
                    .thenReturn(updated);

            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.aktivnostId").value(ACTIVITY_ID))
                    .andExpect(jsonPath("$.naziv").value("Updated Eiffel Tower Visit"))
                    .andExpect(jsonPath("$.opis").value("Updated description"));

            verify(activityService, times(1))
                    .updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class));
        }

        @Test
        @DisplayName("updateActivity_withPartialBody_returns200Ok")
        void updateActivity_withPartialBody_returns200Ok() throws Exception {
            
            UpdateActivityDTO partial = UpdateActivityDTO.builder()
                    .naziv("Renamed Activity")
                    .build();

            when(activityService.updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class)))
                    .thenReturn(activityResponse);

            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partial)))
                    .andExpect(status().isOk());

            verify(activityService, times(1))
                    .updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class));
        }

        @Test
        @DisplayName("updateActivity_withMissingUserIdParam_returns400BadRequest")
        void updateActivity_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("updateActivity_whenEndBeforeStart_propagates400BadRequest")
        void updateActivity_whenEndBeforeStart_propagates400BadRequest() throws Exception {
            
            when(activityService.updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class)))
                    .thenThrow(new IllegalArgumentException("End datetime must be after start datetime"));

            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("End datetime must be after start datetime"));

            verify(activityService, times(1))
                    .updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class));
        }

        @Test
        @DisplayName("updateActivity_whenUserNotParticipant_propagatesAccessDenied")
        void updateActivity_whenUserNotParticipant_propagatesAccessDenied() throws Exception {
            
            when(activityService.updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(activityService, times(1))
                    .updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class));
        }

        @Test
        @DisplayName("updateActivity_whenActivityNotFound_propagatesError")
        void updateActivity_whenActivityNotFound_propagatesError() throws Exception {
            
            when(activityService.updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class)))
                    .thenThrow(new RuntimeException("Activity not found"));

            
            mockMvc.perform(put("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Activity not found"));

            verify(activityService, times(1))
                    .updateActivity(eq(ACTIVITY_ID), eq(USER_ID), any(UpdateActivityDTO.class));
        }
    }

    
    
    

    @Nested
    @DisplayName("DELETE /api/trips/{tripId}/activities/{activityId}")
    class DeleteActivityEndpoint {

        @Test
        @DisplayName("deleteActivity_withValidRequest_returns204NoContent")
        void deleteActivity_withValidRequest_returns204NoContent() throws Exception {
            

            
            mockMvc.perform(delete("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isNoContent());

            verify(activityService, times(1)).deleteActivity(ACTIVITY_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteActivity_withMissingUserIdParam_returns400BadRequest")
        void deleteActivity_withMissingUserIdParam_returns400BadRequest() throws Exception {
            
            mockMvc.perform(delete("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID))
                    .andExpect(status().is4xxClientError());

            verifyNoInteractions(activityService);
        }

        @Test
        @DisplayName("deleteActivity_whenActivityNotFound_propagatesError")
        void deleteActivity_whenActivityNotFound_propagatesError() throws Exception {
            
            org.mockito.Mockito.doThrow(new RuntimeException("Activity not found"))
                    .when(activityService).deleteActivity(ACTIVITY_ID, USER_ID);

            
            mockMvc.perform(delete("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Activity not found"));

            verify(activityService, times(1)).deleteActivity(ACTIVITY_ID, USER_ID);
        }

        @Test
        @DisplayName("deleteActivity_whenUserNotParticipant_propagatesAccessDenied")
        void deleteActivity_whenUserNotParticipant_propagatesAccessDenied() throws Exception {
            
            org.mockito.Mockito.doThrow(new RuntimeException("Access denied: User is not a participant of this trip"))
                    .when(activityService).deleteActivity(ACTIVITY_ID, USER_ID);

            
            mockMvc.perform(delete("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(activityService, times(1)).deleteActivity(ACTIVITY_ID, USER_ID);
        }
    }

    
    
    

    @Nested
    @DisplayName("HTTP method / routing")
    class MethodAndRouting {

        @Test
        @DisplayName("activitiesEndpoint_withWrongHttpMethod_returns405MethodNotAllowed")
        void activitiesEndpoint_withWrongHttpMethod_returns405MethodNotAllowed() throws Exception {
            
            mockMvc.perform(delete("/api/trips/{tripId}/activities", TRIP_ID)
                            .param("userId", USER_ID.toString()))
                    .andExpect(status().isMethodNotAllowed());

            verify(activityService, never()).deleteActivity(any(), any());
        }

        @Test
        @DisplayName("activityResource_withWrongHttpMethod_returns405MethodNotAllowed")
        void activityResource_withWrongHttpMethod_returns405MethodNotAllowed() throws Exception {
            
            mockMvc.perform(post("/api/trips/{tripId}/activities/{activityId}", TRIP_ID, ACTIVITY_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new HashMap<>())))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(activityService);
        }
    }

}
