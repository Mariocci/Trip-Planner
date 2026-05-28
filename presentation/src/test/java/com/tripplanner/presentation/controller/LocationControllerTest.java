package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.LocationService;
import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = LocationController.class,
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
@DisplayName("LocationController Tests")
class LocationControllerTest extends ControllerTestBase {

    @MockBean
    private LocationService locationService;

    private LocationResponseDTO eiffelTower;

    @BeforeEach
    void setUp() {
        eiffelTower = LocationResponseDTO.builder()
                .lokacijaId(1)
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars, 5 Av. Anatole France")
                .grad("Paris")
                .drzava("France")
                .build();
    }

    @Nested
    @DisplayName("GET /api/locations/{id}")
    class GetLocationByIdEndpoint {

        @Test
        @DisplayName("getLocationById_existingId_returns200OkWithLocation")
        void getLocationById_existingId_returns200OkWithLocation() throws Exception {
            
            when(locationService.getLocationById(1)).thenReturn(eiffelTower);

            
            performGet("/api/locations/1")
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.lokacijaId").value(1))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower"))
                    .andExpect(jsonPath("$.adresa").value("Champ de Mars, 5 Av. Anatole France"))
                    .andExpect(jsonPath("$.grad").value("Paris"))
                    .andExpect(jsonPath("$.drzava").value("France"));

            
            verify(locationService, times(1)).getLocationById(1);
        }

        @Test
        @DisplayName("getLocationById_nonExistentId_returns500WhenServiceThrowsRuntimeException")
        void getLocationById_nonExistentId_returns500WhenServiceThrowsRuntimeException() throws Exception {
            
            
            
            
            
            when(locationService.getLocationById(999))
                    .thenThrow(new RuntimeException("Location not found"));

            
            performGet("/api/locations/999")
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Location not found"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(locationService, times(1)).getLocationById(999);
        }

        @Test
        @DisplayName("getLocationById_serviceThrowsIllegalArgument_returns400BadRequest")
        void getLocationById_serviceThrowsIllegalArgument_returns400BadRequest() throws Exception {
            
            when(locationService.getLocationById(-1))
                    .thenThrow(new IllegalArgumentException("Invalid location id"));

            
            performGet("/api/locations/-1")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Invalid location id"));

            verify(locationService, times(1)).getLocationById(-1);
        }

        @Test
        @DisplayName("getLocationById_nonNumericId_isHandledByGlobalExceptionHandler")
        void getLocationById_nonNumericId_isHandledByGlobalExceptionHandler() throws Exception {
            
            
            
            
            performGet("/api/locations/abc")
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            
            verifyNoInteractions(locationService);
        }

        @Test
        @DisplayName("getLocationById_passesPathVariableToServiceUnchanged")
        void getLocationById_passesPathVariableToServiceUnchanged() throws Exception {
            
            LocationResponseDTO another = LocationResponseDTO.builder()
                    .lokacijaId(42)
                    .naziv("Colosseum")
                    .adresa("Piazza del Colosseo, 1")
                    .grad("Rome")
                    .drzava("Italy")
                    .build();
            when(locationService.getLocationById(42)).thenReturn(another);

            
            performGet("/api/locations/42")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lokacijaId").value(42))
                    .andExpect(jsonPath("$.naziv").value("Colosseum"))
                    .andExpect(jsonPath("$.grad").value("Rome"));

            
            verify(locationService).getLocationById(eq(42));
        }

        @Test
        @DisplayName("getLocationById_responseMirrorsServiceOutputIncludingNullFields")
        void getLocationById_responseMirrorsServiceOutputIncludingNullFields() throws Exception {
            
            LocationResponseDTO minimal = LocationResponseDTO.builder()
                    .lokacijaId(7)
                    .naziv("Simple Location")
                    .grad("City")
                    .drzava("Country")
                    .build();
            when(locationService.getLocationById(7)).thenReturn(minimal);

            
            performGet("/api/locations/7")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lokacijaId").value(7))
                    .andExpect(jsonPath("$.naziv").value("Simple Location"))
                    .andExpect(jsonPath("$.adresa").doesNotExist())
                    .andExpect(jsonPath("$.grad").value("City"))
                    .andExpect(jsonPath("$.drzava").value("Country"));

            verify(locationService, times(1)).getLocationById(7);
        }

        @Test
        @DisplayName("getLocationById_withWrongHttpMethod_returns405MethodNotAllowed")
        void getLocationById_withWrongHttpMethod_returns405MethodNotAllowed() throws Exception {
            
            performPut("/api/locations/1", new CreateLocationDTO())
                    .andExpect(status().isMethodNotAllowed());

            
            verify(locationService, never()).getLocationById(any());
        }
    }

    @Nested
    @DisplayName("POST /api/locations - JSON serialization checks")
    class CreateLocationEndpoint {

        @Test
        @DisplayName("createLocation_validRequest_returns201CreatedWithLocation")
        void createLocation_validRequest_returns201CreatedWithLocation() throws Exception {
            
            CreateLocationDTO createDTO = CreateLocationDTO.builder()
                    .naziv("Eiffel Tower")
                    .adresa("Champ de Mars, 5 Av. Anatole France")
                    .grad("Paris")
                    .drzava("France")
                    .build();

            when(locationService.createLocation(any(CreateLocationDTO.class)))
                    .thenReturn(eiffelTower);

            
            performPost("/api/locations", createDTO)
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.lokacijaId").value(1))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower"))
                    .andExpect(jsonPath("$.grad").value("Paris"))
                    .andExpect(jsonPath("$.drzava").value("France"));

            verify(locationService, times(1)).createLocation(any(CreateLocationDTO.class));
        }

        @Test
        @DisplayName("createLocation_missingRequiredFields_returns400BadRequest")
        void createLocation_missingRequiredFields_returns400BadRequest() throws Exception {
            
            CreateLocationDTO invalid = CreateLocationDTO.builder()
                    .adresa("Some address")
                    .build();

            
            performPost("/api/locations", invalid)
                    .andExpect(status().is4xxClientError());

            
            verify(locationService, never()).createLocation(any());
        }
    }
}
