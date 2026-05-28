package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.GooglePlacesService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = PlacesController.class,
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
@DisplayName("PlacesController Tests")
class PlacesControllerTest extends ControllerTestBase {

    @MockBean
    private GooglePlacesService googlePlacesService;

    private List<Map<String, Object>> sampleResults;

    @BeforeEach
    void setUp() {
        sampleResults = new ArrayList<>();

        Map<String, Object> paris = new HashMap<>();
        paris.put("name", "Paris");
        paris.put("address", "Paris, France");
        paris.put("latitude", 48.8566);
        paris.put("longitude", 2.3522);
        paris.put("types", Arrays.asList("locality", "political"));
        sampleResults.add(paris);

        Map<String, Object> eiffel = new HashMap<>();
        eiffel.put("name", "Eiffel Tower");
        eiffel.put("address", "Champ de Mars, 5 Av. Anatole France, 75007 Paris, France");
        eiffel.put("latitude", 48.8584);
        eiffel.put("longitude", 2.2945);
        eiffel.put("types", Arrays.asList("tourist_attraction", "point_of_interest"));
        sampleResults.add(eiffel);
    }

    @Nested
    @DisplayName("GET /api/places/search")
    class SearchPlacesEndpoint {

        @Test
        @DisplayName("searchPlaces_withValidQuery_returns200OkWithResults")
        void searchPlaces_withValidQuery_returns200OkWithResults() throws Exception {
            
            when(googlePlacesService.searchPlaces("Paris")).thenReturn(sampleResults);

            
            performGet("/api/places/search?query=Paris")
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Paris"))
                    .andExpect(jsonPath("$[0].address").value("Paris, France"))
                    .andExpect(jsonPath("$[0].latitude").value(48.8566))
                    .andExpect(jsonPath("$[0].longitude").value(2.3522))
                    .andExpect(jsonPath("$[0].types").isArray())
                    .andExpect(jsonPath("$[0].types[0]").value("locality"))
                    .andExpect(jsonPath("$[1].name").value("Eiffel Tower"))
                    .andExpect(jsonPath("$[1].latitude").value(48.8584));

            
            verify(googlePlacesService, times(1)).searchPlaces("Paris");
        }

        @Test
        @DisplayName("searchPlaces_withEmptyResults_returns200OkWithEmptyArray")
        void searchPlaces_withEmptyResults_returns200OkWithEmptyArray() throws Exception {
            
            when(googlePlacesService.searchPlaces(anyString())).thenReturn(new ArrayList<>());

            
            performGet("/api/places/search?query=NonExistentPlace12345")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(googlePlacesService, times(1)).searchPlaces("NonExistentPlace12345");
        }

        @Test
        @DisplayName("searchPlaces_withMissingQueryParam_returns400BadRequest")
        void searchPlaces_withMissingQueryParam_returns400BadRequest() throws Exception {
            
            
            
            performGet("/api/places/search")
                    .andExpect(status().is4xxClientError());

            
            verifyNoInteractions(googlePlacesService);
        }

        @Test
        @DisplayName("searchPlaces_whenServiceThrowsRuntimeException_returns500InternalServerError")
        void searchPlaces_whenServiceThrowsRuntimeException_returns500InternalServerError() throws Exception {
            
            when(googlePlacesService.searchPlaces(anyString()))
                    .thenThrow(new RuntimeException("Failed to search places: connection timeout"));

            
            performGet("/api/places/search?query=Tokyo")
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Failed to search places: connection timeout"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(googlePlacesService, times(1)).searchPlaces("Tokyo");
        }

        @Test
        @DisplayName("searchPlaces_whenServiceThrowsIllegalArgument_returns400BadRequest")
        void searchPlaces_whenServiceThrowsIllegalArgument_returns400BadRequest() throws Exception {
            
            when(googlePlacesService.searchPlaces(anyString()))
                    .thenThrow(new IllegalArgumentException("Query must not be blank"));

            
            
            mockMvc.perform(get("/api/places/search").param("query", " "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Query must not be blank"));

            verify(googlePlacesService, times(1)).searchPlaces(" ");
        }

        @Test
        @DisplayName("searchPlaces_passesQueryParameterToServiceUnchanged")
        void searchPlaces_passesQueryParameterToServiceUnchanged() throws Exception {
            
            when(googlePlacesService.searchPlaces("New York City")).thenReturn(sampleResults);

            
            mockMvc.perform(get("/api/places/search").param("query", "New York City"))
                    .andExpect(status().isOk());

            
            verify(googlePlacesService).searchPlaces(eq("New York City"));
        }

        @Test
        @DisplayName("searchPlaces_returnsResultsFromServiceWithoutModification")
        void searchPlaces_returnsResultsFromServiceWithoutModification() throws Exception {
            
            Map<String, Object> single = new HashMap<>();
            single.put("name", "Tokyo");
            single.put("address", "Tokyo, Japan");
            single.put("latitude", 35.6762);
            single.put("longitude", 139.6503);
            single.put("types", Arrays.asList("locality"));
            when(googlePlacesService.searchPlaces("Tokyo")).thenReturn(List.of(single));

            
            performGet("/api/places/search?query=Tokyo")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("Tokyo"))
                    .andExpect(jsonPath("$[0].address").value("Tokyo, Japan"))
                    .andExpect(jsonPath("$[0].latitude").value(35.6762))
                    .andExpect(jsonPath("$[0].longitude").value(139.6503))
                    .andExpect(jsonPath("$[0].types[0]").value("locality"));

            verify(googlePlacesService, times(1)).searchPlaces("Tokyo");
        }

        @Test
        @DisplayName("searchPlaces_withWrongHttpMethod_returns405MethodNotAllowed")
        void searchPlaces_withWrongHttpMethod_returns405MethodNotAllowed() throws Exception {
            
            performPost("/api/places/search?query=Paris", new HashMap<>())
                    .andExpect(status().isMethodNotAllowed());

            
            verify(googlePlacesService, never()).searchPlaces(any());
        }
    }
}
