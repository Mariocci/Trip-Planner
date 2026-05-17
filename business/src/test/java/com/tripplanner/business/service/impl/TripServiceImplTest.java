package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TripServiceImpl with mocked repositories.
 * Tests trip creation, updates, deletion, retrieval, authorization checks, and date validation.
 * 
 * Validates Requirements: 2.2, 2.9, 2.10, 2.11, 2.13, 2.14, 2.15
 */
class TripServiceImplTest extends ServiceTestBase {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private Korisnik testUser;
    private Putovanje testTrip;
    private CreateTripDTO createTripDTO;
    private UpdateTripDTO updateTripDTO;

    @BeforeEach
    void setUp() {
        testUser = createTestUser(1, "test@example.com");
        testTrip = createTestTrip(1, "Paris Trip", 
                LocalDate.of(2024, 6, 1), 
                LocalDate.of(2024, 6, 10));
        
        createTripDTO = CreateTripDTO.builder()
                .naziv("Paris Trip")
                .opis("Summer vacation in Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .build();
        
        updateTripDTO = UpdateTripDTO.builder()
                .naziv("Updated Paris Trip")
                .opis("Updated description")
                .datumPoc(LocalDate.of(2024, 6, 2))
                .datumKraj(LocalDate.of(2024, 6, 11))
                .build();
    }

    // ========== Trip Creation Tests ==========

    @Test
    void createTrip_withValidData_createsTrip() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        // When
        TripResponseDTO result = tripService.createTrip(1, createTripDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Paris Trip");
        assertThat(result.getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(result.getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 10));
        assertThat(result.getUkTrosak()).isEqualTo(BigDecimal.ZERO);

        // Verify trip was saved
        ArgumentCaptor<Putovanje> tripCaptor = ArgumentCaptor.forClass(Putovanje.class);
        verify(tripRepository).save(tripCaptor.capture());
        Putovanje savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getNaziv()).isEqualTo("Paris Trip");
        assertThat(savedTrip.getOpis()).isEqualTo("Summer vacation in Paris");
        assertThat(savedTrip.getUkTrosak()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void createTrip_withValidData_addsCreatorAsOrganizer() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        // When
        tripService.createTrip(1, createTripDTO);

        // Then - Verify organizer participant was created
        ArgumentCaptor<Sudionik> participantCaptor = ArgumentCaptor.forClass(Sudionik.class);
        verify(participantRepository).save(participantCaptor.capture());
        Sudionik savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getUloga()).isEqualTo("organizer");
        assertThat(savedParticipant.getKorisnik()).isEqualTo(testUser);
        assertThat(savedParticipant.getPutovanje()).isEqualTo(testTrip);
    }

    @Test
    void createTrip_withInvalidDates_throwsException() {
        // Given - End date before start date
        CreateTripDTO invalidDTO = CreateTripDTO.builder()
                .naziv("Invalid Trip")
                .datumPoc(LocalDate.of(2024, 6, 10))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();

        // When/Then
        assertThatThrownBy(() -> tripService.createTrip(1, invalidDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date must be after or equal to start date");

        // Verify no database operations were performed
        verify(tripRepository, never()).save(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void createTrip_withEqualStartAndEndDates_succeeds() {
        // Given - Same start and end date (valid for single-day trips)
        CreateTripDTO sameDayDTO = CreateTripDTO.builder()
                .naziv("Day Trip")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        // When
        TripResponseDTO result = tripService.createTrip(1, sameDayDTO);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).save(any(Putovanje.class));
    }

    @Test
    void createTrip_withNonExistentUser_throwsException() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.createTrip(999, createTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(tripRepository, never()).save(any());
    }

    // ========== Trip Retrieval Tests ==========

    @Test
    void getTripById_asParticipant_returnsTrip() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(participant));

        // When
        TripResponseDTO result = tripService.getTripById(1, 1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPutovanjeId()).isEqualTo(1);
        assertThat(result.getNaziv()).isEqualTo("Paris Trip");
        assertThat(result.getParticipantCount()).isEqualTo(1);
    }

    @Test
    void getTripById_asNonParticipant_throwsException() {
        // Given
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.getTripById(1, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: User is not a participant of this trip");
    }

    @Test
    void getTripById_withNonExistentTrip_throwsException() {
        // Given
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.getTripById(999, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    @Test
    void listUserTrips_returnsUserTrips() {
        // Given
        Putovanje trip1 = createTestTrip(1, "Trip 1", LocalDate.now(), LocalDate.now().plusDays(5));
        Putovanje trip2 = createTestTrip(2, "Trip 2", LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        
        when(tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(1))
                .thenReturn(Arrays.asList(trip1, trip2));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));
        when(participantRepository.findByPutovanje_PutovanjeId(2)).thenReturn(Arrays.asList(new Sudionik(), new Sudionik()));

        // When
        List<TripResponseDTO> results = tripService.listUserTrips(1);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getNaziv()).isEqualTo("Trip 1");
        assertThat(results.get(0).getParticipantCount()).isEqualTo(1);
        assertThat(results.get(1).getNaziv()).isEqualTo("Trip 2");
        assertThat(results.get(1).getParticipantCount()).isEqualTo(2);
    }

    @Test
    void listUserTrips_withNoTrips_returnsEmptyList() {
        // Given
        when(tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(1))
                .thenReturn(Arrays.asList());

        // When
        List<TripResponseDTO> results = tripService.listUserTrips(1);

        // Then
        assertThat(results).isEmpty();
    }

    // ========== Trip Update Tests ==========

    @Test
    void updateTrip_asOrganizer_updatesTrip() {
        // Given
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(organizer));

        // When
        TripResponseDTO result = tripService.updateTrip(1, 1, updateTripDTO);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).save(any(Putovanje.class));
        
        // Verify trip fields were updated
        assertThat(testTrip.getNaziv()).isEqualTo("Updated Paris Trip");
        assertThat(testTrip.getOpis()).isEqualTo("Updated description");
        assertThat(testTrip.getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 2));
        assertThat(testTrip.getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 11));
    }

    @Test
    void updateTrip_asParticipant_throwsException() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        // When/Then
        assertThatThrownBy(() -> tripService.updateTrip(1, 1, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can update trips");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_asNonParticipant_throwsException() {
        // Given
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.updateTrip(1, 2, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can update trips");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_withPartialUpdate_updatesOnlyProvidedFields() {
        // Given
        UpdateTripDTO partialUpdate = UpdateTripDTO.builder()
                .naziv("New Name Only")
                .build();
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(organizer));

        LocalDate originalStartDate = testTrip.getDatumPoc();
        LocalDate originalEndDate = testTrip.getDatumKraj();
        String originalDescription = testTrip.getOpis();

        // When
        tripService.updateTrip(1, 1, partialUpdate);

        // Then
        assertThat(testTrip.getNaziv()).isEqualTo("New Name Only");
        assertThat(testTrip.getOpis()).isEqualTo(originalDescription);
        assertThat(testTrip.getDatumPoc()).isEqualTo(originalStartDate);
        assertThat(testTrip.getDatumKraj()).isEqualTo(originalEndDate);
    }

    @Test
    void updateTrip_withInvalidDates_throwsException() {
        // Given
        UpdateTripDTO invalidUpdate = UpdateTripDTO.builder()
                .datumPoc(LocalDate.of(2024, 6, 10))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));

        // When/Then
        assertThatThrownBy(() -> tripService.updateTrip(1, 1, invalidUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date must be after or equal to start date");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_withNonExistentTrip_throwsException() {
        // Given
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(999, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.updateTrip(999, 1, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    // ========== Trip Deletion Tests ==========

    @Test
    void deleteTrip_asOrganizer_deletesTrip() {
        // Given
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        // When
        tripService.deleteTrip(1, 1);

        // Then
        verify(tripRepository).deleteById(1);
    }

    @Test
    void deleteTrip_asParticipant_throwsException() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        // When/Then
        assertThatThrownBy(() -> tripService.deleteTrip(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can delete trips");

        verify(tripRepository, never()).deleteById(any());
    }

    @Test
    void deleteTrip_asNonParticipant_throwsException() {
        // Given
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.deleteTrip(1, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can delete trips");

        verify(tripRepository, never()).deleteById(any());
    }

    // ========== Authorization Tests ==========

    @Test
    void isUserOrganizer_withOrganizer_returnsTrue() {
        // Given
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        // When
        boolean result = tripService.isUserOrganizer(1, 1);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isUserOrganizer_withParticipant_returnsFalse() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        // When
        boolean result = tripService.isUserOrganizer(1, 1);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isUserOrganizer_withNonParticipant_returnsFalse() {
        // Given
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        // When
        boolean result = tripService.isUserOrganizer(1, 2);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isUserParticipant_withParticipant_returnsTrue() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        // When
        boolean result = tripService.isUserParticipant(1, 1);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isUserParticipant_withOrganizer_returnsTrue() {
        // Given
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        // When
        boolean result = tripService.isUserParticipant(1, 1);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isUserParticipant_withNonParticipant_returnsFalse() {
        // Given
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        // When
        boolean result = tripService.isUserParticipant(1, 2);

        // Then
        assertThat(result).isFalse();
    }

    // ========== Expense Recalculation Tests ==========

    @Test
    void recalculateTotalExpense_withExpenses_updatesTripTotal() {
        // Given
        BigDecimal totalExpenses = new BigDecimal("250.50");
        when(expenseRepository.sumByPutovanjeId(1)).thenReturn(totalExpenses);
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);

        // When
        tripService.recalculateTotalExpense(1);

        // Then
        verify(expenseRepository).sumByPutovanjeId(1);
        verify(tripRepository).save(testTrip);
        assertThat(testTrip.getUkTrosak()).isEqualTo(totalExpenses);
    }

    @Test
    void recalculateTotalExpense_withNoExpenses_setsZero() {
        // Given
        when(expenseRepository.sumByPutovanjeId(1)).thenReturn(null);
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);

        // When
        tripService.recalculateTotalExpense(1);

        // Then
        verify(expenseRepository).sumByPutovanjeId(1);
        verify(tripRepository).save(testTrip);
        assertThat(testTrip.getUkTrosak()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void recalculateTotalExpense_withNonExistentTrip_throwsException() {
        // Given
        when(expenseRepository.sumByPutovanjeId(999)).thenReturn(BigDecimal.ZERO);
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tripService.recalculateTotalExpense(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");

        verify(tripRepository, never()).save(any());
    }

    // ========== Mock Interaction Verification Tests ==========

    @Test
    void createTrip_verifiesCorrectRepositoryInteractions() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        // When
        tripService.createTrip(1, createTripDTO);

        // Then - Verify exact sequence of repository calls
        verify(userRepository).findById(1);
        verify(tripRepository).save(any(Putovanje.class));
        verify(participantRepository).save(any(Sudionik.class));
        verify(participantRepository).findByPutovanje_PutovanjeId(1);
    }

    @Test
    void getTripById_verifiesCorrectRepositoryInteractions() {
        // Given
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(participant));

        // When
        tripService.getTripById(1, 1);

        // Then
        verify(tripRepository).findById(1);
        verify(participantRepository).findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1);
        verify(participantRepository).findByPutovanje_PutovanjeId(1);
    }
}
