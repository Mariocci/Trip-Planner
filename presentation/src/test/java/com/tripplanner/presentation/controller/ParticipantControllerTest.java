package com.tripplanner.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tripplanner.business.service.ParticipantService;
import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;
import com.tripplanner.domain.dto.UserResponseDTO;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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


@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipantController Unit Tests")
class ParticipantControllerTest extends ControllerTestBase {

    @Mock
    private ParticipantService participantService;

    private ParticipantController participantController;

    private static final String BASE_URL = "/api/trips/{tripId}/participants";
    private static final String ROLE_URL = "/api/trips/{tripId}/participants/{participantId}/role";
    private static final String PARTICIPANT_URL = "/api/trips/{tripId}/participants/{participantId}";

    private static final Integer TRIP_ID = 1;
    private static final Integer ORGANIZER_ID = 10;
    private static final Integer PARTICIPANT_ID = 100;
    private static final Integer USER_ID = 20;

    private ParticipantResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        participantController = new ParticipantController(participantService);

        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(participantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(jacksonConverter)
                .build();

        UserResponseDTO user = UserResponseDTO.builder()
                .korisnikId(USER_ID)
                .ime("Marko")
                .prezime("Markovic")
                .email("marko@example.com")
                .oauthProvider("google")
                .build();

        sampleResponse = ParticipantResponseDTO.builder()
                .sudionikId(PARTICIPANT_ID)
                .uloga("PARTICIPANT")
                .user(user)
                .build();
    }

    
    
    

    @Nested
    @DisplayName("POST /api/trips/{tripId}/participants")
    class AddParticipant {

        @Test
        @DisplayName("addParticipant_validRequest_returns201Created")
        void addParticipant_validRequest_returns201Created() throws Exception {
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("marko@example.com")
                    .uloga("PARTICIPANT")
                    .build();

            when(participantService.addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sudionikId").value(PARTICIPANT_ID))
                    .andExpect(jsonPath("$.uloga").value("PARTICIPANT"))
                    .andExpect(jsonPath("$.user.korisnikId").value(USER_ID))
                    .andExpect(jsonPath("$.user.email").value("marko@example.com"))
                    .andExpect(jsonPath("$.user.ime").value("Marko"))
                    .andExpect(jsonPath("$.user.prezime").value("Markovic"));

            
            ArgumentCaptor<AddParticipantDTO> captor = ArgumentCaptor.forClass(AddParticipantDTO.class);
            verify(participantService, times(1))
                    .addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), captor.capture());
            AddParticipantDTO captured = captor.getValue();
            assertThat(captured.getEmail()).isEqualTo("marko@example.com");
            assertThat(captured.getUloga()).isEqualTo("PARTICIPANT");
        }

        @Test
        @DisplayName("addParticipant_missingEmail_returns400BadRequest")
        void addParticipant_missingEmail_returns400BadRequest() throws Exception {
            
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email(null)
                    .uloga("PARTICIPANT")
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("addParticipant_blankEmail_returns400BadRequest")
        void addParticipant_blankEmail_returns400BadRequest() throws Exception {
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("   ")
                    .uloga("PARTICIPANT")
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("addParticipant_invalidEmailFormat_returns400BadRequest")
        void addParticipant_invalidEmailFormat_returns400BadRequest() throws Exception {
            
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("not-an-email")
                    .uloga("PARTICIPANT")
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("addParticipant_missingUloga_returns400BadRequest")
        void addParticipant_missingUloga_returns400BadRequest() throws Exception {
            
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("marko@example.com")
                    .uloga(null)
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("addParticipant_missingRequestingUserIdParam_returns400BadRequest")
        void addParticipant_missingRequestingUserIdParam_returns400BadRequest() throws Exception {
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("marko@example.com")
                    .uloga("PARTICIPANT")
                    .build();

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("addParticipant_nonOrganizerRequest_propagatesServiceError")
        void addParticipant_nonOrganizerRequest_propagatesServiceError() throws Exception {
            
            
            
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("marko@example.com")
                    .uloga("PARTICIPANT")
                    .build();

            when(participantService.addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: Only organizers can add participants"));

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can add participants"));

            verify(participantService, times(1))
                    .addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class));
        }

        @Test
        @DisplayName("addParticipant_userNotFound_propagatesServiceError")
        void addParticipant_userNotFound_propagatesServiceError() throws Exception {
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("missing@example.com")
                    .uloga("PARTICIPANT")
                    .build();

            when(participantService.addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class)))
                    .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("User not found"));

            verify(participantService, times(1))
                    .addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class));
        }

        @Test
        @DisplayName("addParticipant_duplicateParticipant_returns400BadRequest")
        void addParticipant_duplicateParticipant_returns400BadRequest() throws Exception {
            AddParticipantDTO request = AddParticipantDTO.builder()
                    .email("marko@example.com")
                    .uloga("PARTICIPANT")
                    .build();

            when(participantService.addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class)))
                    .thenThrow(new IllegalArgumentException("User is already a participant"));

            mockMvc.perform(post(BASE_URL, TRIP_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User is already a participant"));

            verify(participantService, times(1))
                    .addParticipant(eq(TRIP_ID), eq(ORGANIZER_ID), any(AddParticipantDTO.class));
        }
    }

    
    
    

    @Nested
    @DisplayName("GET /api/trips/{tripId}/participants")
    class ListTripParticipants {

        @Test
        @DisplayName("listTripParticipants_validRequest_returns200OkWithParticipantList")
        void listTripParticipants_validRequest_returns200OkWithParticipantList() throws Exception {
            UserResponseDTO secondUser = UserResponseDTO.builder()
                    .korisnikId(21)
                    .ime("Ana")
                    .prezime("Anic")
                    .email("ana@example.com")
                    .oauthProvider("google")
                    .build();
            ParticipantResponseDTO second = ParticipantResponseDTO.builder()
                    .sudionikId(101)
                    .uloga("ORGANIZER")
                    .user(secondUser)
                    .build();
            List<ParticipantResponseDTO> participants = Arrays.asList(sampleResponse, second);

            when(participantService.listTripParticipants(TRIP_ID, ORGANIZER_ID)).thenReturn(participants);

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].sudionikId").value(PARTICIPANT_ID))
                    .andExpect(jsonPath("$[0].uloga").value("PARTICIPANT"))
                    .andExpect(jsonPath("$[0].user.email").value("marko@example.com"))
                    .andExpect(jsonPath("$[1].sudionikId").value(101))
                    .andExpect(jsonPath("$[1].uloga").value("ORGANIZER"))
                    .andExpect(jsonPath("$[1].user.email").value("ana@example.com"));

            verify(participantService, times(1)).listTripParticipants(TRIP_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("listTripParticipants_noParticipants_returns200OkWithEmptyList")
        void listTripParticipants_noParticipants_returns200OkWithEmptyList() throws Exception {
            when(participantService.listTripParticipants(TRIP_ID, ORGANIZER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(participantService, times(1)).listTripParticipants(TRIP_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("listTripParticipants_userNotParticipant_propagatesServiceError")
        void listTripParticipants_userNotParticipant_propagatesServiceError() throws Exception {
            when(participantService.listTripParticipants(TRIP_ID, ORGANIZER_ID))
                    .thenThrow(new RuntimeException("Access denied: User is not a participant of this trip"));

            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .param("userId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: User is not a participant of this trip"));

            verify(participantService, times(1)).listTripParticipants(TRIP_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("listTripParticipants_missingUserIdParam_returns400BadRequest")
        void listTripParticipants_missingUserIdParam_returns400BadRequest() throws Exception {
            mockMvc.perform(get(BASE_URL, TRIP_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }
    }

    
    
    
    

    @Nested
    @DisplayName("PUT /api/trips/{tripId}/participants/{participantId}/role")
    class UpdateParticipantRole {

        @Test
        @DisplayName("updateParticipantRole_validRequest_returns200OkWithUpdatedParticipant")
        void updateParticipantRole_validRequest_returns200OkWithUpdatedParticipant() throws Exception {
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga("ORGANIZER")
                    .build();

            UserResponseDTO user = UserResponseDTO.builder()
                    .korisnikId(USER_ID)
                    .ime("Marko")
                    .prezime("Markovic")
                    .email("marko@example.com")
                    .oauthProvider("google")
                    .build();
            ParticipantResponseDTO updated = ParticipantResponseDTO.builder()
                    .sudionikId(PARTICIPANT_ID)
                    .uloga("ORGANIZER")
                    .user(user)
                    .build();

            when(participantService.updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID),
                    any(UpdateParticipantRoleDTO.class)))
                    .thenReturn(updated);

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sudionikId").value(PARTICIPANT_ID))
                    .andExpect(jsonPath("$.uloga").value("ORGANIZER"))
                    .andExpect(jsonPath("$.user.korisnikId").value(USER_ID));

            ArgumentCaptor<UpdateParticipantRoleDTO> captor =
                    ArgumentCaptor.forClass(UpdateParticipantRoleDTO.class);
            verify(participantService, times(1))
                    .updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID), captor.capture());
            UpdateParticipantRoleDTO captured = captor.getValue();
            assertThat(captured.getUloga()).isEqualTo("ORGANIZER");
        }

        @Test
        @DisplayName("updateParticipantRole_missingUloga_returns400BadRequest")
        void updateParticipantRole_missingUloga_returns400BadRequest() throws Exception {
            
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga(null)
                    .build();

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("updateParticipantRole_blankUloga_returns400BadRequest")
        void updateParticipantRole_blankUloga_returns400BadRequest() throws Exception {
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga("   ")
                    .build();

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("updateParticipantRole_missingRequestingUserIdParam_returns400BadRequest")
        void updateParticipantRole_missingRequestingUserIdParam_returns400BadRequest() throws Exception {
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga("ORGANIZER")
                    .build();

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("updateParticipantRole_nonOrganizerRequest_propagatesServiceError")
        void updateParticipantRole_nonOrganizerRequest_propagatesServiceError() throws Exception {
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga("ORGANIZER")
                    .build();

            when(participantService.updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID),
                    any(UpdateParticipantRoleDTO.class)))
                    .thenThrow(new RuntimeException("Access denied: Only organizers can update roles"));

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can update roles"));

            verify(participantService, times(1))
                    .updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID),
                            any(UpdateParticipantRoleDTO.class));
        }

        @Test
        @DisplayName("updateParticipantRole_participantNotFound_propagatesServiceError")
        void updateParticipantRole_participantNotFound_propagatesServiceError() throws Exception {
            UpdateParticipantRoleDTO request = UpdateParticipantRoleDTO.builder()
                    .uloga("ORGANIZER")
                    .build();

            when(participantService.updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID),
                    any(UpdateParticipantRoleDTO.class)))
                    .thenThrow(new RuntimeException("Participant not found"));

            mockMvc.perform(put(ROLE_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Participant not found"));

            verify(participantService, times(1))
                    .updateParticipantRole(eq(PARTICIPANT_ID), eq(ORGANIZER_ID),
                            any(UpdateParticipantRoleDTO.class));
        }
    }

    
    
    
    

    @Nested
    @DisplayName("DELETE /api/trips/{tripId}/participants/{participantId}")
    class RemoveParticipant {

        @Test
        @DisplayName("removeParticipant_validRequest_returns204NoContent")
        void removeParticipant_validRequest_returns204NoContent() throws Exception {
            doNothing().when(participantService).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);

            mockMvc.perform(delete(PARTICIPANT_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(participantService, times(1)).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("removeParticipant_nonOrganizerRequest_propagatesServiceError")
        void removeParticipant_nonOrganizerRequest_propagatesServiceError() throws Exception {
            doThrow(new RuntimeException("Access denied: Only organizers can remove participants"))
                    .when(participantService).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);

            mockMvc.perform(delete(PARTICIPANT_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Access denied: Only organizers can remove participants"));

            verify(participantService, times(1)).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("removeParticipant_lastOrganizer_returns400BadRequest")
        void removeParticipant_lastOrganizer_returns400BadRequest() throws Exception {
            
            doThrow(new IllegalArgumentException("Cannot remove the last organizer of a trip"))
                    .when(participantService).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);

            mockMvc.perform(delete(PARTICIPANT_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cannot remove the last organizer of a trip"));

            verify(participantService, times(1)).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("removeParticipant_participantNotFound_propagatesServiceError")
        void removeParticipant_participantNotFound_propagatesServiceError() throws Exception {
            doThrow(new RuntimeException("Participant not found"))
                    .when(participantService).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);

            mockMvc.perform(delete(PARTICIPANT_URL, TRIP_ID, PARTICIPANT_ID)
                            .param("requestingUserId", ORGANIZER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Participant not found"));

            verify(participantService, times(1)).removeParticipant(PARTICIPANT_ID, ORGANIZER_ID);
        }

        @Test
        @DisplayName("removeParticipant_missingRequestingUserIdParam_returns400BadRequest")
        void removeParticipant_missingRequestingUserIdParam_returns400BadRequest() throws Exception {
            mockMvc.perform(delete(PARTICIPANT_URL, TRIP_ID, PARTICIPANT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(participantService, never()).removeParticipant(any(), any());
        }
    }
}
