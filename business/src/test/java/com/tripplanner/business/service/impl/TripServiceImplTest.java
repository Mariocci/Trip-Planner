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

    

    @Test
    void createTrip_withValidData_createsTrip() {
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        
        TripResponseDTO result = tripService.createTrip(1, createTripDTO);

        
        assertThat(result).isNotNull();
        assertThat(result.getNaziv()).isEqualTo("Paris Trip");
        assertThat(result.getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(result.getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 10));
        assertThat(result.getUkTrosak()).isEqualTo(BigDecimal.ZERO);

        
        ArgumentCaptor<Putovanje> tripCaptor = ArgumentCaptor.forClass(Putovanje.class);
        verify(tripRepository).save(tripCaptor.capture());
        Putovanje savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getNaziv()).isEqualTo("Paris Trip");
        assertThat(savedTrip.getOpis()).isEqualTo("Summer vacation in Paris");
        assertThat(savedTrip.getUkTrosak()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void createTrip_withValidData_addsCreatorAsOrganizer() {
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        
        tripService.createTrip(1, createTripDTO);

        
        ArgumentCaptor<Sudionik> participantCaptor = ArgumentCaptor.forClass(Sudionik.class);
        verify(participantRepository).save(participantCaptor.capture());
        Sudionik savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getUloga()).isEqualTo("organizer");
        assertThat(savedParticipant.getKorisnik()).isEqualTo(testUser);
        assertThat(savedParticipant.getPutovanje()).isEqualTo(testTrip);
    }

    @Test
    void createTrip_withInvalidDates_throwsException() {
        
        CreateTripDTO invalidDTO = CreateTripDTO.builder()
                .naziv("Invalid Trip")
                .datumPoc(LocalDate.of(2024, 6, 10))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();

        
        assertThatThrownBy(() -> tripService.createTrip(1, invalidDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date must be after or equal to start date");

        
        verify(tripRepository, never()).save(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void createTrip_withEqualStartAndEndDates_succeeds() {
        
        CreateTripDTO sameDayDTO = CreateTripDTO.builder()
                .naziv("Day Trip")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        
        TripResponseDTO result = tripService.createTrip(1, sameDayDTO);

        
        assertThat(result).isNotNull();
        verify(tripRepository).save(any(Putovanje.class));
    }

    @Test
    void createTrip_withNonExistentUser_throwsException() {
        
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.createTrip(999, createTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(tripRepository, never()).save(any());
    }

    

    @Test
    void getTripById_asParticipant_returnsTrip() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(participant));

        
        TripResponseDTO result = tripService.getTripById(1, 1);

        
        assertThat(result).isNotNull();
        assertThat(result.getPutovanjeId()).isEqualTo(1);
        assertThat(result.getNaziv()).isEqualTo("Paris Trip");
        assertThat(result.getParticipantCount()).isEqualTo(1);
    }

    @Test
    void getTripById_asNonParticipant_throwsException() {
        
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.getTripById(1, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: User is not a participant of this trip");
    }

    @Test
    void getTripById_withNonExistentTrip_throwsException() {
        
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.getTripById(999, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    @Test
    void listUserTrips_returnsUserTrips() {
        
        Putovanje trip1 = createTestTrip(1, "Trip 1", LocalDate.now(), LocalDate.now().plusDays(5));
        Putovanje trip2 = createTestTrip(2, "Trip 2", LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        
        when(tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(1))
                .thenReturn(Arrays.asList(trip1, trip2));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));
        when(participantRepository.findByPutovanje_PutovanjeId(2)).thenReturn(Arrays.asList(new Sudionik(), new Sudionik()));

        
        List<TripResponseDTO> results = tripService.listUserTrips(1);

        
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getNaziv()).isEqualTo("Trip 1");
        assertThat(results.get(0).getParticipantCount()).isEqualTo(1);
        assertThat(results.get(1).getNaziv()).isEqualTo("Trip 2");
        assertThat(results.get(1).getParticipantCount()).isEqualTo(2);
    }

    @Test
    void listUserTrips_withNoTrips_returnsEmptyList() {
        
        when(tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(1))
                .thenReturn(Arrays.asList());

        
        List<TripResponseDTO> results = tripService.listUserTrips(1);

        
        assertThat(results).isEmpty();
    }

    

    @Test
    void updateTrip_asOrganizer_updatesTrip() {
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(organizer));

        
        TripResponseDTO result = tripService.updateTrip(1, 1, updateTripDTO);

        
        assertThat(result).isNotNull();
        verify(tripRepository).save(any(Putovanje.class));
        
        
        assertThat(testTrip.getNaziv()).isEqualTo("Updated Paris Trip");
        assertThat(testTrip.getOpis()).isEqualTo("Updated description");
        assertThat(testTrip.getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 2));
        assertThat(testTrip.getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 11));
    }

    @Test
    void updateTrip_asParticipant_throwsException() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        
        assertThatThrownBy(() -> tripService.updateTrip(1, 1, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can update trips");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_asNonParticipant_throwsException() {
        
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.updateTrip(1, 2, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can update trips");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_withPartialUpdate_updatesOnlyProvidedFields() {
        
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

        
        tripService.updateTrip(1, 1, partialUpdate);

        
        assertThat(testTrip.getNaziv()).isEqualTo("New Name Only");
        assertThat(testTrip.getOpis()).isEqualTo(originalDescription);
        assertThat(testTrip.getDatumPoc()).isEqualTo(originalStartDate);
        assertThat(testTrip.getDatumKraj()).isEqualTo(originalEndDate);
    }

    @Test
    void updateTrip_withInvalidDates_throwsException() {
        
        UpdateTripDTO invalidUpdate = UpdateTripDTO.builder()
                .datumPoc(LocalDate.of(2024, 6, 10))
                .datumKraj(LocalDate.of(2024, 6, 1))
                .build();
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));

        
        assertThatThrownBy(() -> tripService.updateTrip(1, 1, invalidUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date must be after or equal to start date");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void updateTrip_withNonExistentTrip_throwsException() {
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(999, 1))
                .thenReturn(Optional.of(organizer));
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.updateTrip(999, 1, updateTripDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    

    @Test
    void deleteTrip_asOrganizer_deletesTrip() {
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        
        tripService.deleteTrip(1, 1);

        
        verify(tripRepository).deleteById(1);
    }

    @Test
    void deleteTrip_asParticipant_throwsException() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        
        assertThatThrownBy(() -> tripService.deleteTrip(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can delete trips");

        verify(tripRepository, never()).deleteById(any());
    }

    @Test
    void deleteTrip_asNonParticipant_throwsException() {
        
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.deleteTrip(1, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied: Only organizers can delete trips");

        verify(tripRepository, never()).deleteById(any());
    }

    

    @Test
    void isUserOrganizer_withOrganizer_returnsTrue() {
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        
        boolean result = tripService.isUserOrganizer(1, 1);

        
        assertThat(result).isTrue();
    }

    @Test
    void isUserOrganizer_withParticipant_returnsFalse() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        
        boolean result = tripService.isUserOrganizer(1, 1);

        
        assertThat(result).isFalse();
    }

    @Test
    void isUserOrganizer_withNonParticipant_returnsFalse() {
        
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        
        boolean result = tripService.isUserOrganizer(1, 2);

        
        assertThat(result).isFalse();
    }

    @Test
    void isUserParticipant_withParticipant_returnsTrue() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));

        
        boolean result = tripService.isUserParticipant(1, 1);

        
        assertThat(result).isTrue();
    }

    @Test
    void isUserParticipant_withOrganizer_returnsTrue() {
        
        Sudionik organizer = createTestParticipant(1, testTrip, testUser, "organizer");
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(organizer));

        
        boolean result = tripService.isUserParticipant(1, 1);

        
        assertThat(result).isTrue();
    }

    @Test
    void isUserParticipant_withNonParticipant_returnsFalse() {
        
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 2))
                .thenReturn(Optional.empty());

        
        boolean result = tripService.isUserParticipant(1, 2);

        
        assertThat(result).isFalse();
    }

    

    @Test
    void recalculateTotalExpense_withExpenses_updatesTripTotal() {
        
        BigDecimal totalExpenses = new BigDecimal("250.50");
        when(expenseRepository.sumByPutovanjeId(1)).thenReturn(totalExpenses);
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);

        
        tripService.recalculateTotalExpense(1);

        
        verify(expenseRepository).sumByPutovanjeId(1);
        verify(tripRepository).save(testTrip);
        assertThat(testTrip.getUkTrosak()).isEqualTo(totalExpenses);
    }

    @Test
    void recalculateTotalExpense_withNoExpenses_setsZero() {
        
        when(expenseRepository.sumByPutovanjeId(1)).thenReturn(null);
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);

        
        tripService.recalculateTotalExpense(1);

        
        verify(expenseRepository).sumByPutovanjeId(1);
        verify(tripRepository).save(testTrip);
        assertThat(testTrip.getUkTrosak()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void recalculateTotalExpense_withNonExistentTrip_throwsException() {
        
        when(expenseRepository.sumByPutovanjeId(999)).thenReturn(BigDecimal.ZERO);
        when(tripRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> tripService.recalculateTotalExpense(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");

        verify(tripRepository, never()).save(any());
    }

    

    @Test
    void createTrip_verifiesCorrectRepositoryInteractions() {
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Putovanje.class))).thenReturn(testTrip);
        when(participantRepository.save(any(Sudionik.class))).thenReturn(new Sudionik());
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(new Sudionik()));

        
        tripService.createTrip(1, createTripDTO);

        
        verify(userRepository).findById(1);
        verify(tripRepository).save(any(Putovanje.class));
        verify(participantRepository).save(any(Sudionik.class));
        verify(participantRepository).findByPutovanje_PutovanjeId(1);
    }

    @Test
    void getTripById_verifiesCorrectRepositoryInteractions() {
        
        Sudionik participant = createTestParticipant(1, testTrip, testUser, "participant");
        when(tripRepository.findById(1)).thenReturn(Optional.of(testTrip));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findByPutovanje_PutovanjeId(1)).thenReturn(Arrays.asList(participant));

        
        tripService.getTripById(1, 1);

        
        verify(tripRepository).findById(1);
        verify(participantRepository).findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(1, 1);
        verify(participantRepository).findByPutovanje_PutovanjeId(1);
    }
}
