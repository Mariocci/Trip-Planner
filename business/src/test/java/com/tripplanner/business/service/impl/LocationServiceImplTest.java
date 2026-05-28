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


class LocationServiceImplTest extends ServiceTestBase {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Lokacija testLocation;
    private CreateLocationDTO createLocationDTO;

    @BeforeEach
    void setUp() {
        
        testLocation = createTestLocation(1, "Eiffel Tower", "Champ de Mars", "Paris", "France");
        
        
        createLocationDTO = CreateLocationDTO.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build();
    }

    

    @Test
    void createLocation_WithValidData_ShouldCreateLocation() {
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(testLocation);

        
        LocationResponseDTO result = locationService.createLocation(createLocationDTO);

        
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

        
        LocationResponseDTO result = locationService.createLocation(minimalDTO);

        
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

        
        LocationResponseDTO result = locationService.createLocation(specialDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Café de l'Opéra");
        assertThat(result.getAdresa()).isEqualTo("123 Rue de la Paix");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    

    @Test
    void getLocationById_WithExistingId_ShouldReturnLocation() {
        
        when(locationRepository.findById(1)).thenReturn(Optional.of(testLocation));

        
        LocationResponseDTO result = locationService.getLocationById(1);

        
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
        
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> locationService.getLocationById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(999);
    }

    @Test
    void getLocationById_WithNullId_ShouldThrowException() {
        
        when(locationRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        
        assertThatThrownBy(() -> locationService.getLocationById(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        verify(locationRepository).findById(null);
    }

    

    @Test
    void createLocation_WithGooglePlacesId_ShouldStoreCorrectly() {
        
        
        
        
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

        
        LocationResponseDTO result = locationService.createLocation(googleDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Google Place Location");
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    

    @Test
    void searchLocations_WithMatchingQuery_ShouldReturnMatchingLocations() {
        
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Eiffel Tower", "Address 2", "Paris", "France");
        Lokacija location3 = createTestLocation(3, "London Bridge", "Address 3", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        
        List<LocationResponseDTO> results = locationService.searchLocations("Paris");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(LocationResponseDTO::getNaziv)
                .containsExactlyInAnyOrder("Paris Museum", "Eiffel Tower");
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithCaseInsensitiveQuery_ShouldReturnMatchingLocations() {
        
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Eiffel Tower", "Address 2", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        
        List<LocationResponseDTO> results = locationService.searchLocations("PARIS");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithNoMatches_ShouldReturnEmptyList() {
        
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1));

        
        List<LocationResponseDTO> results = locationService.searchLocations("Tokyo");

        
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_WithEmptyQuery_ShouldReturnAllLocations() {
        
        Lokacija location1 = createTestLocation(1, "Paris Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "London Bridge", "Address 2", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        
        List<LocationResponseDTO> results = locationService.searchLocations("");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_MatchingByCity_ShouldReturnMatchingLocations() {
        
        Lokacija location1 = createTestLocation(1, "Museum", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Tower", "Address 2", "Paris", "France");
        Lokacija location3 = createTestLocation(3, "Bridge", "Address 3", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        
        List<LocationResponseDTO> results = locationService.searchLocations("London");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGrad()).isEqualTo("London");
        
        verify(locationRepository).findAll();
    }

    @Test
    void searchLocations_MatchingByName_ShouldReturnMatchingLocations() {
        
        Lokacija location1 = createTestLocation(1, "Eiffel Tower", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Tower Bridge", "Address 2", "London", "UK");
        Lokacija location3 = createTestLocation(3, "Museum", "Address 3", "Paris", "France");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2, location3));

        
        List<LocationResponseDTO> results = locationService.searchLocations("Tower");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(LocationResponseDTO::getNaziv)
                .containsExactlyInAnyOrder("Eiffel Tower", "Tower Bridge");
        
        verify(locationRepository).findAll();
    }

    

    @Test
    void createLocation_ShouldCallRepositorySaveOnce() {
        
        when(locationRepository.save(any(Lokacija.class))).thenReturn(testLocation);

        
        locationService.createLocation(createLocationDTO);

        
        verify(locationRepository, times(1)).save(any(Lokacija.class));
        verifyNoMoreInteractions(locationRepository);
    }

    @Test
    void getLocationById_ShouldCallRepositoryFindByIdOnce() {
        
        when(locationRepository.findById(1)).thenReturn(Optional.of(testLocation));

        
        locationService.getLocationById(1);

        
        verify(locationRepository, times(1)).findById(1);
        verifyNoMoreInteractions(locationRepository);
    }

    @Test
    void searchLocations_ShouldCallRepositoryFindAllOnce() {
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(testLocation));

        
        locationService.searchLocations("Paris");

        
        verify(locationRepository, times(1)).findAll();
        verifyNoMoreInteractions(locationRepository);
    }

    

    @Test
    void createLocation_WithLongNames_ShouldCreateLocation() {
        
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

        
        LocationResponseDTO result = locationService.createLocation(longDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).hasSize(255);
        
        verify(locationRepository).save(any(Lokacija.class));
    }

    @Test
    void searchLocations_WithPartialMatch_ShouldReturnMatchingLocations() {
        
        Lokacija location1 = createTestLocation(1, "Paris Museum of Art", "Address 1", "Paris", "France");
        Lokacija location2 = createTestLocation(2, "Museum of London", "Address 2", "London", "UK");
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        
        List<LocationResponseDTO> results = locationService.searchLocations("Museum");

        
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        
        verify(locationRepository).findAll();
    }

    @Test
    void getLocationById_WithZeroId_ShouldThrowException() {
        
        when(locationRepository.findById(0)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> locationService.getLocationById(0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(0);
    }

    @Test
    void getLocationById_WithNegativeId_ShouldThrowException() {
        
        when(locationRepository.findById(-1)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> locationService.getLocationById(-1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
        
        verify(locationRepository).findById(-1);
    }
}
