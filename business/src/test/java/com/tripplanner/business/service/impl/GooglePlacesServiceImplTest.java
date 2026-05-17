package com.tripplanner.business.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GooglePlacesServiceImpl}.
 * 
 * **Validates: Requirements 2.8, 2.9, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7**
 */
@ExtendWith(MockitoExtension.class)
class GooglePlacesServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private GooglePlacesServiceImpl googlePlacesService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        googlePlacesService = new GooglePlacesServiceImpl();
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(googlePlacesService, "restTemplate", restTemplate);
    }

    @Test
    void searchPlaces_WithValidQuery_ShouldReturnParsedLocations() {
        // Given
        String query = "Paris, France";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Paris"
                  },
                  "formattedAddress": "Paris, France",
                  "location": {
                    "latitude": 48.8566,
                    "longitude": 2.3522
                  },
                  "types": ["locality", "political"]
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        
        Map<String, Object> place = results.get(0);
        assertThat(place.get("name")).isEqualTo("Paris");
        assertThat(place.get("address")).isEqualTo("Paris, France");
        assertThat(place.get("latitude")).isEqualTo(48.8566);
        assertThat(place.get("longitude")).isEqualTo(2.3522);
        assertThat(place.get("types")).isInstanceOf(List.class);
        
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) place.get("types");
        assertThat(types).containsExactly("locality", "political");
    }

    @Test
    void searchPlaces_WithMultipleResults_ShouldReturnAllParsedLocations() {
        // Given
        String query = "Zagreb";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Zagreb"
                  },
                  "formattedAddress": "Zagreb, Croatia",
                  "location": {
                    "latitude": 45.8150,
                    "longitude": 15.9819
                  },
                  "types": ["locality", "political"]
                },
                {
                  "displayName": {
                    "text": "Zagreb County"
                  },
                  "formattedAddress": "Zagreb County, Croatia",
                  "location": {
                    "latitude": 45.9000,
                    "longitude": 16.0000
                  },
                  "types": ["administrative_area_level_1", "political"]
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("name")).isEqualTo("Zagreb");
        assertThat(results.get(1).get("name")).isEqualTo("Zagreb County");
    }

    @Test
    void searchPlaces_ShouldSortCitiesFirst() {
        // Given
        String query = "New York";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "New York Hotel"
                  },
                  "formattedAddress": "123 Main St, New York, NY",
                  "location": {
                    "latitude": 40.7128,
                    "longitude": -74.0060
                  },
                  "types": ["lodging", "point_of_interest"]
                },
                {
                  "displayName": {
                    "text": "New York"
                  },
                  "formattedAddress": "New York, NY, USA",
                  "location": {
                    "latitude": 40.7128,
                    "longitude": -74.0060
                  },
                  "types": ["locality", "political"]
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        // City should be sorted first
        assertThat(results.get(0).get("name")).isEqualTo("New York");
        assertThat(results.get(1).get("name")).isEqualTo("New York Hotel");
    }

    @Test
    void searchPlaces_WithEmptyResults_ShouldReturnEmptyList() {
        // Given
        String query = "NonExistentPlace12345";
        String mockResponse = """
            {
              "places": []
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void searchPlaces_WithNoPlacesField_ShouldReturnEmptyList() {
        // Given
        String query = "Test";
        String mockResponse = """
            {
              "status": "OK"
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void searchPlaces_ShouldFormatRequestCorrectly() {
        // Given
        String query = "London";
        String mockResponse = """
            {
              "places": []
            }
            """;

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        googlePlacesService.searchPlaces(query);

        // Then
        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).isEqualTo("https://places.googleapis.com/v1/places:searchText");

        HttpEntity<?> capturedRequest = requestCaptor.getValue();
        HttpHeaders headers = capturedRequest.getHeaders();
        
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.get("X-Goog-Api-Key")).containsExactly("test-api-key");
        assertThat(headers.get("X-Goog-FieldMask"))
                .containsExactly("places.displayName,places.formattedAddress,places.location,places.types");

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) capturedRequest.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("textQuery")).isEqualTo(query);
    }

    @Test
    void searchPlaces_WithNetworkError_ShouldThrowRuntimeException() {
        // Given
        String query = "Paris";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new ResourceAccessException("Connection timeout"));

        // When/Then
        assertThatThrownBy(() -> googlePlacesService.searchPlaces(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to search places")
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    void searchPlaces_WithInvalidApiKey_ShouldThrowRuntimeException() {
        // Given
        String query = "Paris";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid API key"));

        // When/Then
        assertThatThrownBy(() -> googlePlacesService.searchPlaces(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to search places")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void searchPlaces_WithRateLimiting_ShouldThrowRuntimeException() {
        // Given
        String query = "Paris";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));

        // When/Then
        assertThatThrownBy(() -> googlePlacesService.searchPlaces(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to search places")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void searchPlaces_WithServerError_ShouldThrowRuntimeException() {
        // Given
        String query = "Paris";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        // When/Then
        assertThatThrownBy(() -> googlePlacesService.searchPlaces(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to search places")
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void searchPlaces_WithMalformedResponse_ShouldThrowRuntimeException() {
        // Given
        String query = "Paris";
        String malformedResponse = "{ invalid json }";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(malformedResponse, HttpStatus.OK));

        // When/Then
        assertThatThrownBy(() -> googlePlacesService.searchPlaces(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse places response");
    }

    @Test
    void searchPlaces_WithPartialData_ShouldHandleGracefully() {
        // Given
        String query = "Test";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Place Without Address"
                  },
                  "location": {
                    "latitude": 45.0,
                    "longitude": 15.0
                  }
                },
                {
                  "formattedAddress": "Address Without Name",
                  "location": {
                    "latitude": 46.0,
                    "longitude": 16.0
                  }
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        // First place has name but no address
        assertThat(results.get(0).get("name")).isEqualTo("Place Without Address");
        assertThat(results.get(0).get("address")).isNull();
        assertThat(results.get(0).get("latitude")).isEqualTo(45.0);
        
        // Second place has address but no name
        assertThat(results.get(1).get("name")).isNull();
        assertThat(results.get(1).get("address")).isEqualTo("Address Without Name");
        assertThat(results.get(1).get("latitude")).isEqualTo(46.0);
    }

    @Test
    void searchPlaces_WithAllFieldsPresent_ShouldParseAllFields() {
        // Given
        String query = "Complete Place";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Complete Place Name"
                  },
                  "formattedAddress": "123 Main St, City, Country",
                  "location": {
                    "latitude": 40.7128,
                    "longitude": -74.0060
                  },
                  "types": ["locality", "political", "geocode"]
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        
        Map<String, Object> place = results.get(0);
        assertThat(place).containsKeys("name", "address", "latitude", "longitude", "types");
        assertThat(place.get("name")).isEqualTo("Complete Place Name");
        assertThat(place.get("address")).isEqualTo("123 Main St, City, Country");
        assertThat(place.get("latitude")).isEqualTo(40.7128);
        assertThat(place.get("longitude")).isEqualTo(-74.0060);
        
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) place.get("types");
        assertThat(types).containsExactly("locality", "political", "geocode");
    }

    @Test
    void searchPlaces_ShouldVerifyRestTemplateInteraction() {
        // Given
        String query = "Test Query";
        String mockResponse = """
            {
              "places": []
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        googlePlacesService.searchPlaces(query);

        // Then
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void searchPlaces_WithNullLocation_ShouldHandleGracefully() {
        // Given
        String query = "Test";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Place Without Location"
                  },
                  "formattedAddress": "Some Address"
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("name")).isEqualTo("Place Without Location");
        assertThat(results.get(0).get("latitude")).isNull();
        assertThat(results.get(0).get("longitude")).isNull();
    }

    @Test
    void searchPlaces_WithEmptyTypes_ShouldHandleGracefully() {
        // Given
        String query = "Test";
        String mockResponse = """
            {
              "places": [
                {
                  "displayName": {
                    "text": "Place With Empty Types"
                  },
                  "formattedAddress": "Some Address",
                  "location": {
                    "latitude": 45.0,
                    "longitude": 15.0
                  },
                  "types": []
                }
              ]
            }
            """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) results.get(0).get("types");
        assertThat(types).isEmpty();
    }
}
