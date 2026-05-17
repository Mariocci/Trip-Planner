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

/**
 * Unit tests for {@link LocationController}.
 *
 * <p>Tests the controller in isolation by mocking {@link LocationService}.
 * Focuses on the {@code GET /api/locations/{id}} endpoint as required by the
 * task, but also includes checks for HTTP status codes, JSON serialization
 * (request and response), and verification of mock interactions with the
 * service layer.</p>
 *
 * <p>Security filters are disabled for these tests so the controller logic
 * can be exercised without requiring a valid JWT token.</p>
 *
 * <p>Validates: Requirements 3.7, 3.9, 3.10, 3.11, 3.12, 3.13</p>
 */
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
            // Given - service returns the requested location
            when(locationService.getLocationById(1)).thenReturn(eiffelTower);

            // When/Then - HTTP 200 OK with serialized location DTO
            performGet("/api/locations/1")
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.lokacijaId").value(1))
                    .andExpect(jsonPath("$.naziv").value("Eiffel Tower"))
                    .andExpect(jsonPath("$.adresa").value("Champ de Mars, 5 Av. Anatole France"))
                    .andExpect(jsonPath("$.grad").value("Paris"))
                    .andExpect(jsonPath("$.drzava").value("France"));

            // Verify service was called exactly once with the path id
            verify(locationService, times(1)).getLocationById(1);
        }

        @Test
        @DisplayName("getLocationById_nonExistentId_returns500WhenServiceThrowsRuntimeException")
        void getLocationById_nonExistentId_returns500WhenServiceThrowsRuntimeException() throws Exception {
            // Given - the production LocationServiceImpl throws RuntimeException
            // ("Location not found") when the entity is missing.
            // The GlobalExceptionHandler maps RuntimeException -> 500.
            // This test documents the current contract between controller,
            // service, and global handler.
            when(locationService.getLocationById(999))
                    .thenThrow(new RuntimeException("Location not found"));

            // When/Then
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
            // Given - service rejects the supplied id with an IllegalArgumentException
            when(locationService.getLocationById(-1))
                    .thenThrow(new IllegalArgumentException("Invalid location id"));

            // When/Then - GlobalExceptionHandler maps IllegalArgumentException -> 400
            performGet("/api/locations/-1")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Invalid location id"));

            verify(locationService, times(1)).getLocationById(-1);
        }

        @Test
        @DisplayName("getLocationById_nonNumericId_isHandledByGlobalExceptionHandler")
        void getLocationById_nonNumericId_isHandledByGlobalExceptionHandler() throws Exception {
            // When/Then - Spring fails to convert "abc" to Integer and raises
            // MethodArgumentTypeMismatchException (a RuntimeException). The
            // current GlobalExceptionHandler maps RuntimeException -> 500 with
            // a descriptive message. This test documents that contract.
            performGet("/api/locations/abc")
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            // Service must never be invoked for an unparseable path variable
            verifyNoInteractions(locationService);
        }

        @Test
        @DisplayName("getLocationById_passesPathVariableToServiceUnchanged")
        void getLocationById_passesPathVariableToServiceUnchanged() throws Exception {
            // Given
            LocationResponseDTO another = LocationResponseDTO.builder()
                    .lokacijaId(42)
                    .naziv("Colosseum")
                    .adresa("Piazza del Colosseo, 1")
                    .grad("Rome")
                    .drzava("Italy")
                    .build();
            when(locationService.getLocationById(42)).thenReturn(another);

            // When
            performGet("/api/locations/42")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lokacijaId").value(42))
                    .andExpect(jsonPath("$.naziv").value("Colosseum"))
                    .andExpect(jsonPath("$.grad").value("Rome"));

            // Then - controller forwards the path variable verbatim
            verify(locationService).getLocationById(eq(42));
        }

        @Test
        @DisplayName("getLocationById_responseMirrorsServiceOutputIncludingNullFields")
        void getLocationById_responseMirrorsServiceOutputIncludingNullFields() throws Exception {
            // Given - location with optional 'adresa' field unset
            LocationResponseDTO minimal = LocationResponseDTO.builder()
                    .lokacijaId(7)
                    .naziv("Simple Location")
                    .grad("City")
                    .drzava("Country")
                    .build();
            when(locationService.getLocationById(7)).thenReturn(minimal);

            // When/Then - all fields including null 'adresa' are serialized
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
            // When/Then - PUT is not mapped on /api/locations/{id}
            performPut("/api/locations/1", new CreateLocationDTO())
                    .andExpect(status().isMethodNotAllowed());

            // Service must not be called for unsupported methods
            verify(locationService, never()).getLocationById(any());
        }
    }

    @Nested
    @DisplayName("POST /api/locations - JSON serialization checks")
    class CreateLocationEndpoint {

        @Test
        @DisplayName("createLocation_validRequest_returns201CreatedWithLocation")
        void createLocation_validRequest_returns201CreatedWithLocation() throws Exception {
            // Given - valid creation payload, service returns persisted location
            CreateLocationDTO createDTO = CreateLocationDTO.builder()
                    .naziv("Eiffel Tower")
                    .adresa("Champ de Mars, 5 Av. Anatole France")
                    .grad("Paris")
                    .drzava("France")
                    .build();

            when(locationService.createLocation(any(CreateLocationDTO.class)))
                    .thenReturn(eiffelTower);

            // When/Then - request body is deserialized, response body is serialized
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
            // Given - body missing @NotBlank naziv/grad/drzava
            CreateLocationDTO invalid = CreateLocationDTO.builder()
                    .adresa("Some address")
                    .build();

            // When/Then - bean validation triggers a 4xx response
            performPost("/api/locations", invalid)
                    .andExpect(status().is4xxClientError());

            // Service must not be invoked when validation fails
            verify(locationService, never()).createLocation(any());
        }
    }
}
