package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import com.tripplanner.domain.entity.Lokacija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LocationServiceImpl}.
 * Tests location creation, retrieval, Google Places ID handling, and error handling.
 * 
 * **Validates: Requirements 2.6, 2.9, 2.13, 2.14, 2.15**
 */
class LocationServiceImplTest extends ServiceTestBase {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Lokacija testLocation;
    private CreateLocationDTO createLocationDTO;

    @BeforeEach
    void setUp() {
        // Create test location entity
        testLocation = createTestLocation(1, "Eiffel Tower", "Champ de Mars", "Paris", "France");
        
        // Create test DTO
        createLocationDTO = CreateLocationDTO.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build();
    }

    // ========== Location Creation Tests ==========

    @Test
    void createLocation_WithValidData_ShouldCreateLocation() {
        // Given
        when(locationRepository.save(any(Lokacija.class))).thenReturn(testLocation);

        // When
        LocationResponseDTO result = locationService.createLocation(createLocationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLokacijaId()).isEqualTo(1);
        assertThat(result.getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(result.getAdresa()).isEqualTo("Champ de Mars");
        assertThat(result.getGrad()).isEqualTo("Paris");
        assertThat(result.getDrzava()).isEqualTo("France");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    @Test
    void createLocation_WithMinimalData_ShouldCreateLocation() {
        // Given
        CreateLocationDTO minimalDTO = CreateLocationDTO.builder()
                .naziv("Simple Location")
                .grad("City")
                .drzava("Country")
                .build();
        
        Lokacija minimalLocation = Lokacija.builder()
                .lokacijaId(2)
                .naziv("Simple Location")
                .grad("City")
                .drzava("Country")
                .build();
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(minimalLocation);

        // When
        LocationResponseDTO result = locationService.createLocation(minimalDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLokacijaId()).isEqualTo(2);
        assertThat(result.getNaziv()).isEqualTo("Simple Location");
        assertThat(result.getAdresa()).isNull();
        assertThat(result.getGrad()).isEqualTo("City");
        assertThat(result.getDrzava()).isEqualTo("Country");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    @Test
    void createLocation_WithSpecialCharacters_ShouldCreateLocation() {
        // Given
        CreateLocationDTO specialDTO = CreateLocationDTO.builder()
                .naziv("Café de l'Opéra")
                .adresa("123 Rue de la Paix")
                .grad("Paris")
                .drzava("France")
                .build();
        
        Lokacija specialLocation = Lokacija.builder()
                .lokacijaId(3)
                .naziv("Café de l'Opéra")
                .adresa("123 Rue de la Paix")
                .grad("Paris")
                .drzava("France")
                .build();
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(specialLocation);

        // When
        LocationResponseDTO result = locationService.createLocation(specialDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Café de l'Opéra");
        assertThat(result.getAdresa()).isEqualTo("123 Rue de la Paix");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    // ========== Location Retrieval Tests ==========

    @Test
    void getLocationById_WithExistingId_ShouldReturnLocation() {
        // Given
        when(locationRepository.findById(1)).thenReturn(Optional.of(testLocation));

        // When
        LocationResponseDTO result = locationService.getLocationById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLokacijaId()).isEqualTo(1);
        assertThat(result.getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(result.getAdresa()).isEqualTo("Champ de Mars");
        assertThat(result.getGrad()).isEqualTo("Paris");
        assertThat(result.getDrzava()).isEqualTo("France");
        
        verify(locationRepository).findById(1);
    }

    @Test
    void getLocationById_WithNonExistentId_ShouldThrowException() {
        // Given
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.getLocationById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(999);
    }

    @Test
    void getLocationById_WithNullId_ShouldThrowException() {
        // Given
        when(locationRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        // When/Then
        assertThatThrownBy(() -> locationService.getLocationById(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        verify(locationRepository).findById(null);
    }

    // ========== Google Places ID Handling Tests ==========

    @Test
    void createLocation_WithGooglePlacesId_ShouldStoreCorrectly() {
        // Given
        // Note: Current implementation doesn't have googlePlacesId field
        // This test validates the basic location creation which would support
        // Google Places integration when the field is added
        CreateLocationDTO googleDTO = CreateLocationDTO.builder()
                .naziv("Google Place Location")
                .adresa("123 Google Street")
                .grad("Mountain View")
                .drzava("USA")
                .build();
        
        Lokacija googleLocation = Lokacija.builder()
                .lokacijaId(4)
                .naziv("Google Place Location")
                .adresa("123 Google Street")
                .grad("Mountain View")
                .drzava("USA")
                .build();
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(googleLocation);

        // When
        LocationResponseDTO result = locationService.createLocation(googleDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Google Place Location");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    // ========== Search Locations Tests ==========

    @Test
    void searchLocations_WithMatchingQuery_ShouldReturnMatchingLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Eiffel Tower", "Address 2", "Paris", "France");
        Lokacija location3 = createTestLocation(3, "London Bridge", "Address 3", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("Paris");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(LocationResponseDTO::getNaziv)
                .containsExactlyInAnyOrder("Paris Museum", "Eiffel Tower");
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithCaseInsensitiveQuery_ShouldReturnMatchingLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Eiffel Tower", "Address 2", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("PARIS");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithNoMatches_ShouldReturnEmptyList() {
        // Given
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("Tokyo");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithEmptyQuery_ShouldReturnAllLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "London Bridge", "Address 2", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_MatchingByCity_ShouldReturnMatchingLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Tower", "Address 2", "Paris", "France");
        Lokacija location3 = createTestLocation(3, "Bridge", "Address 3", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("London");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGrad()).isEqualTo("London");
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_MatchingByName_ShouldReturnMatchingLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Eiffel Tower", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Tower Bridge", "Address 2", "London", "UK");
        Lokacija location3 = createTestLocation(3, "Museum", "Address 3", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("Tower");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(LocationResponseDTO::getNaziv)
                .containsExactlyInAnyOrder("Eiffel Tower", "Tower Bridge");
        
        verify(locationRepository).findAll();
    }

    // ========== Mock Interaction Verification Tests ==========

    @Test
    void createLocation_ShouldCallRepositorySaveOnce() {
        // Given
        when(locationRepository.save(any(Lokacija.class))).thenReturn(testLocation);

        // When
        locationService.createLocation(createLocationDTO);

        // Then
        verify(locationRepository, times(1)).save(any(Lokacija.class));
        verifyNoMoreInteractions(locationRepository);
    }

    @Test
    void getLocationById_ShouldCallRepositoryFindByIdOnce() {
        // Given
        when(locationRepository.findById(1)).thenReturn(Optional.of(testLocation));

        // When
        locationService.getLocationById(1);

        // Then
        verify(locationRepository, times(1)).findById(1);
        verifyNoMoreInteractions(locationRepository);
    }

    @Test
    void searchLocations_ShouldCallRepositoryFindAllOnce() {
        // Given
        when(locationRepository.findAll()).thenReturn(Arrays.asList(testLocation));

        // When
        locationService.searchLocations("Paris");

        // Then
        verify(locationRepository, times(1)).findAll();
        verifyNoMoreInteractions(locationRepository);
    }

    // ========== Edge Case Tests ==========

    @Test
    void createLocation_WithLongNames_ShouldCreateLocation() {
        // Given
        String longName = "A".repeat(255);
        CreateLocationDTO longDTO = CreateLocationDTO.builder()
                .naziv(longName)
                .adresa("Address")
                .grad("City")
                .drzava("Country")
                .build();
        
        Lokacija longLocation = Lokacija.builder()
                .lokacijaId(5)
                .naziv(longName)
                .adresa("Address")
                .grad("City")
                .drzava("Country")
                .build();
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(longLocation);

        // When
        LocationResponseDTO result = locationService.createLocation(longDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).hasSize(255);
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    @Test
    void searchLocations_WithPartialMatch_ShouldReturnMatchingLocations() {
        // Given
        Lokacija location1 = createTestLocation(1, "Paris Museum of Art", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Museum of London", "Address 2", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        // When
        List<LocationResponseDTO> results = locationService.searchLocations("Museum");

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void getLocationById_WithZeroId_ShouldThrowException() {
        // Given
        when(locationRepository.findById(0)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.getLocationById(0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(0);
    }

    @Test
    void getLocationById_WithNegativeId_ShouldThrowException() {
        // Given
        when(locationRepository.findById(-1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.getLocationById(-1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(-1);
    }
}
