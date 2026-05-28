package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ActivityRepository;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;
import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;


class ActivityServiceImplTest extends ServiceTestBase {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TripService tripService;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private Putovanje testTrip;
    private Lokacija testLocation;
    private Kategorija testCategory1;
    private Kategorija testCategory2;
    private Aktivnost testActivity;
    private CreateActivityDTO createActivityDTO;
    private UpdateActivityDTO updateActivityDTO;
    private Integer userId;
    private Integer tripId;

    @BeforeEach
    void setUp() {
        userId = 1;
        tripId = 1;

        
        testTrip = createTestTrip();
        testLocation = createTestLocation();
        testCategory1 = createTestCategory(1, "Sightseeing");
        testCategory2 = createTestCategory(2, "Food");

        testActivity = createTestActivity(testTrip, testLocation);
        testActivity.setCategories(new ArrayList<>(Arrays.asList(testCategory1)));

        
        createActivityDTO = CreateActivityDTO.builder()
                .naziv("Museum Visit")
                .opis("Visit the Louvre Museum")
                .datumVrijemePoc(LocalDateTime.now().plusDays(2))
                .datumVrijemeKraj(LocalDateTime.now().plusDays(2).plusHours(3))
                .lokacijaId(testLocation.getLokacijaId())
                .categoryIds(Arrays.asList(1))
                .build();

        updateActivityDTO = UpdateActivityDTO.builder()
                .naziv("Updated Activity")
                .opis("Updated description")
                .build();
    }

    

    @Test
    void createActivity_withValidData_createsActivitySuccessfully() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.of(testLocation));
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory1));
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        ActivityResponseDTO result = activityService.createActivity(tripId, userId, createActivityDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo(testActivity.getNaziv());
        assertThat(result.getOpis()).isEqualTo(testActivity.getOpis());
        assertThat(result.getLocation()).isNotNull();
        assertThat(result.getLocation().getLokacijaId()).isEqualTo(testLocation.getLokacijaId());
        assertThat(result.getCategories()).hasSize(1);

        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository).findById(tripId);
        verify(locationRepository).findById(testLocation.getLokacijaId());
        verify(categoryRepository).findAllById(anyList());
        verify(activityRepository).save(any(Aktivnost.class));
    }

    @Test
    void createActivity_withLocationAndCategoryAssociations_associatesCorrectly() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.of(testLocation));
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory1, testCategory2));
        
        ArgumentCaptor<Aktivnost> activityCaptor = ArgumentCaptor.forClass(Aktivnost.class);
        when(activityRepository.save(activityCaptor.capture())).thenReturn(testActivity);

        createActivityDTO.setCategoryIds(Arrays.asList(1, 2));

        
        activityService.createActivity(tripId, userId, createActivityDTO);

        
        Aktivnost savedActivity = activityCaptor.getValue();
        assertThat(savedActivity.getLokacija()).isEqualTo(testLocation);
        assertThat(savedActivity.getCategories()).hasSize(2);
        assertThat(savedActivity.getPutovanje()).isEqualTo(testTrip);

        verify(locationRepository).findById(testLocation.getLokacijaId());
        verify(categoryRepository).findAllById(Arrays.asList(1, 2));
    }

    @Test
    void createActivity_withNoCategoryIds_createsActivityWithEmptyCategories() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.of(testLocation));
        
        ArgumentCaptor<Aktivnost> activityCaptor = ArgumentCaptor.forClass(Aktivnost.class);
        when(activityRepository.save(activityCaptor.capture())).thenReturn(testActivity);

        createActivityDTO.setCategoryIds(null);

        
        activityService.createActivity(tripId, userId, createActivityDTO);

        
        Aktivnost savedActivity = activityCaptor.getValue();
        assertThat(savedActivity.getCategories()).isEmpty();

        verify(categoryRepository, never()).findAllById(anyList());
    }

    @Test
    void createActivity_withEmptyCategoryIds_createsActivityWithEmptyCategories() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.of(testLocation));
        
        ArgumentCaptor<Aktivnost> activityCaptor = ArgumentCaptor.forClass(Aktivnost.class);
        when(activityRepository.save(activityCaptor.capture())).thenReturn(testActivity);

        createActivityDTO.setCategoryIds(Collections.emptyList());

        
        activityService.createActivity(tripId, userId, createActivityDTO);

        
        Aktivnost savedActivity = activityCaptor.getValue();
        assertThat(savedActivity.getCategories()).isEmpty();

        verify(categoryRepository, never()).findAllById(anyList());
    }

    @Test
    void createActivity_whenUserNotParticipant_throwsException() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
        assertThatThrownBy(() -> activityService.createActivity(tripId, userId, createActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository, never()).findById(anyInt());
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void createActivity_whenEndDateTimeBeforeStartDateTime_throwsException() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        
        createActivityDTO.setDatumVrijemePoc(LocalDateTime.now().plusDays(2));
        createActivityDTO.setDatumVrijemeKraj(LocalDateTime.now().plusDays(1)); 

        
        assertThatThrownBy(() -> activityService.createActivity(tripId, userId, createActivityDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End datetime must be after start datetime");

        verify(tripService).isUserParticipant(tripId, userId);
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void createActivity_whenTripNotFound_throwsException() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.createActivity(tripId, userId, createActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip not found");

        verify(tripRepository).findById(tripId);
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void createActivity_whenLocationNotFound_throwsException() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.createActivity(tripId, userId, createActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");

        verify(locationRepository).findById(testLocation.getLokacijaId());
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    

    @Test
    void getActivityById_withValidId_returnsActivity() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);

        
        ActivityResponseDTO result = activityService.getActivityById(activityId, userId);

        
        assertThat(result).isNotNull();
        assertThat(result.getAktivnostId()).isEqualTo(activityId);
        assertThat(result.getNaziv()).isEqualTo(testActivity.getNaziv());
        assertThat(result.getLocation()).isNotNull();

        verify(activityRepository).findById(activityId);
        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), userId);
    }

    @Test
    void getActivityById_whenActivityNotFound_throwsException() {
        
        Integer activityId = 999;
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.getActivityById(activityId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Activity not found");

        verify(activityRepository).findById(activityId);
        verify(tripService, never()).isUserParticipant(anyInt(), anyInt());
    }

    @Test
    void getActivityById_whenUserNotParticipant_throwsException() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(false);

        
        assertThatThrownBy(() -> activityService.getActivityById(activityId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(activityRepository).findById(activityId);
        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), userId);
    }

    

    @Test
    void listTripActivities_withValidTripId_returnsActivities() {
        
        Aktivnost activity2 = createTestActivity(2, "Second Activity", testTrip, testLocation,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2));
        activity2.setCategories(new ArrayList<>());

        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId))
                .thenReturn(Arrays.asList(testActivity, activity2));

        
        List<ActivityResponseDTO> result = activityService.listTripActivities(tripId, userId);

        
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAktivnostId()).isEqualTo(testActivity.getAktivnostId());
        assertThat(result.get(1).getAktivnostId()).isEqualTo(activity2.getAktivnostId());

        verify(tripService).isUserParticipant(tripId, userId);
        verify(activityRepository).findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId);
    }

    @Test
    void listTripActivities_whenNoActivities_returnsEmptyList() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId))
                .thenReturn(Collections.emptyList());

        
        List<ActivityResponseDTO> result = activityService.listTripActivities(tripId, userId);

        
        assertThat(result).isEmpty();

        verify(tripService).isUserParticipant(tripId, userId);
        verify(activityRepository).findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId);
    }

    @Test
    void listTripActivities_whenUserNotParticipant_throwsException() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
        assertThatThrownBy(() -> activityService.listTripActivities(tripId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(tripService).isUserParticipant(tripId, userId);
        verify(activityRepository, never()).findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(anyInt());
    }

    

    @Test
    void updateActivity_withValidData_updatesActivitySuccessfully() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        ActivityResponseDTO result = activityService.updateActivity(activityId, userId, updateActivityDTO);

        
        assertThat(result).isNotNull();
        verify(activityRepository).findById(activityId);
        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), userId);
        verify(activityRepository).save(testActivity);
    }

    @Test
    void updateActivity_withAllFields_updatesAllFields() {
        
        Integer activityId = testActivity.getAktivnostId();
        Lokacija newLocation = createTestLocation(2, "New Location", "456 New St", "New City", "New Country");
        
        updateActivityDTO.setNaziv("Updated Name");
        updateActivityDTO.setOpis("Updated Description");
        updateActivityDTO.setDatumVrijemePoc(LocalDateTime.now().plusDays(5));
        updateActivityDTO.setDatumVrijemeKraj(LocalDateTime.now().plusDays(5).plusHours(4));
        updateActivityDTO.setLokacijaId(newLocation.getLokacijaId());
        updateActivityDTO.setCategoryIds(Arrays.asList(1, 2));

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(locationRepository.findById(newLocation.getLokacijaId())).thenReturn(Optional.of(newLocation));
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory1, testCategory2));
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        activityService.updateActivity(activityId, userId, updateActivityDTO);

        
        assertThat(testActivity.getNaziv()).isEqualTo("Updated Name");
        assertThat(testActivity.getOpis()).isEqualTo("Updated Description");
        assertThat(testActivity.getLokacija()).isEqualTo(newLocation);
        assertThat(testActivity.getCategories()).hasSize(2);

        verify(locationRepository).findById(newLocation.getLokacijaId());
        verify(categoryRepository).findAllById(Arrays.asList(1, 2));
        verify(activityRepository).save(testActivity);
    }

    @Test
    void updateActivity_withPartialFields_updatesOnlyProvidedFields() {
        
        Integer activityId = testActivity.getAktivnostId();
        String originalOpis = testActivity.getOpis();
        
        UpdateActivityDTO partialUpdate = UpdateActivityDTO.builder()
                .naziv("Only Name Updated")
                .build();

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        activityService.updateActivity(activityId, userId, partialUpdate);

        
        assertThat(testActivity.getNaziv()).isEqualTo("Only Name Updated");
        assertThat(testActivity.getOpis()).isEqualTo(originalOpis); 

        verify(locationRepository, never()).findById(anyInt());
        verify(categoryRepository, never()).findAllById(anyList());
    }

    @Test
    void updateActivity_whenEndDateTimeBeforeStartDateTime_throwsException() {
        
        Integer activityId = testActivity.getAktivnostId();
        
        updateActivityDTO.setDatumVrijemePoc(LocalDateTime.now().plusDays(5));
        updateActivityDTO.setDatumVrijemeKraj(LocalDateTime.now().plusDays(4)); 

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);

        
        assertThatThrownBy(() -> activityService.updateActivity(activityId, userId, updateActivityDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End datetime must be after start datetime");

        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void updateActivity_whenActivityNotFound_throwsException() {
        
        Integer activityId = 999;
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.updateActivity(activityId, userId, updateActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Activity not found");

        verify(activityRepository).findById(activityId);
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void updateActivity_whenUserNotParticipant_throwsException() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(false);

        
        assertThatThrownBy(() -> activityService.updateActivity(activityId, userId, updateActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    @Test
    void updateActivity_whenLocationNotFound_throwsException() {
        
        Integer activityId = testActivity.getAktivnostId();
        updateActivityDTO.setLokacijaId(999);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(locationRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.updateActivity(activityId, userId, updateActivityDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");

        verify(locationRepository).findById(999);
        verify(activityRepository, never()).save(any(Aktivnost.class));
    }

    

    @Test
    void deleteActivity_withValidId_deletesActivitySuccessfully() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);

        
        activityService.deleteActivity(activityId, userId);

        
        verify(activityRepository).findById(activityId);
        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), userId);
        verify(activityRepository).delete(testActivity);
    }

    @Test
    void deleteActivity_whenActivityNotFound_throwsException() {
        
        Integer activityId = 999;
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> activityService.deleteActivity(activityId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Activity not found");

        verify(activityRepository).findById(activityId);
        verify(activityRepository, never()).delete(any(Aktivnost.class));
    }

    @Test
    void deleteActivity_whenUserNotParticipant_throwsException() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(false);

        
        assertThatThrownBy(() -> activityService.deleteActivity(activityId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(activityRepository).findById(activityId);
        verify(activityRepository, never()).delete(any(Aktivnost.class));
    }

    

    @Test
    void createActivity_verifiesAllRepositoryInteractions() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findById(testLocation.getLokacijaId())).thenReturn(Optional.of(testLocation));
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory1));
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        activityService.createActivity(tripId, userId, createActivityDTO);

        
        verify(tripService, times(1)).isUserParticipant(tripId, userId);
        verify(tripRepository, times(1)).findById(tripId);
        verify(locationRepository, times(1)).findById(testLocation.getLokacijaId());
        verify(categoryRepository, times(1)).findAllById(anyList());
        verify(activityRepository, times(1)).save(any(Aktivnost.class));
        
        verifyNoMoreInteractions(tripService, tripRepository, locationRepository, categoryRepository, activityRepository);
    }

    @Test
    void updateActivity_verifiesRepositoryInteractionsForLocationUpdate() {
        
        Integer activityId = testActivity.getAktivnostId();
        Lokacija newLocation = createTestLocation(2, "New Location", "456 New St", "New City", "New Country");
        updateActivityDTO.setLokacijaId(newLocation.getLokacijaId());

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(locationRepository.findById(newLocation.getLokacijaId())).thenReturn(Optional.of(newLocation));
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        activityService.updateActivity(activityId, userId, updateActivityDTO);

        
        verify(activityRepository, times(1)).findById(activityId);
        verify(tripService, times(1)).isUserParticipant(testTrip.getPutovanjeId(), userId);
        verify(locationRepository, times(1)).findById(newLocation.getLokacijaId());
        verify(activityRepository, times(1)).save(testActivity);
        
        verifyNoMoreInteractions(activityRepository, tripService, locationRepository);
    }

    @Test
    void updateActivity_verifiesRepositoryInteractionsForCategoryUpdate() {
        
        Integer activityId = testActivity.getAktivnostId();
        updateActivityDTO.setCategoryIds(Arrays.asList(1, 2));

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);
        when(categoryRepository.findAllById(anyList())).thenReturn(Arrays.asList(testCategory1, testCategory2));
        when(activityRepository.save(any(Aktivnost.class))).thenReturn(testActivity);

        
        activityService.updateActivity(activityId, userId, updateActivityDTO);

        
        verify(activityRepository, times(1)).findById(activityId);
        verify(tripService, times(1)).isUserParticipant(testTrip.getPutovanjeId(), userId);
        verify(categoryRepository, times(1)).findAllById(Arrays.asList(1, 2));
        verify(activityRepository, times(1)).save(testActivity);
        
        verifyNoMoreInteractions(activityRepository, tripService, categoryRepository);
    }

    @Test
    void deleteActivity_verifiesAllRepositoryInteractions() {
        
        Integer activityId = testActivity.getAktivnostId();
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(testActivity));
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), userId)).thenReturn(true);

        
        activityService.deleteActivity(activityId, userId);

        
        verify(activityRepository, times(1)).findById(activityId);
        verify(tripService, times(1)).isUserParticipant(testTrip.getPutovanjeId(), userId);
        verify(activityRepository, times(1)).delete(testActivity);
        
        verifyNoMoreInteractions(activityRepository, tripService);
    }
}
