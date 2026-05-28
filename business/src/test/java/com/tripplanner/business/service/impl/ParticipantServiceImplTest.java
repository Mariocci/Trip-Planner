package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class ParticipantServiceImplTest extends ServiceTestBase {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripService tripService;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private Korisnik organizerUser;
    private Korisnik participantUser;
    private Korisnik newUser;
    private Putovanje testTrip;
    private Sudionik organizerParticipant;
    private Sudionik regularParticipant;

    @BeforeEach
    void setUp() {
        
        organizerUser = createTestUser(1, "organizer@example.com");
        participantUser = createTestUser(2, "participant@example.com");
        newUser = createTestUser(3, "newuser@example.com");

        
        testTrip = createTestTrip();

        
        organizerParticipant = createTestParticipant(1, testTrip, organizerUser, "organizer");
        regularParticipant = createTestParticipant(2, testTrip, participantUser, "participant");
    }

    

    @Test
    void addParticipant_withOrganizerAccess_addsParticipantSuccessfully() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("newuser@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(newUser));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), newUser.getKorisnikId()))
                .thenReturn(Optional.empty());

        Sudionik savedParticipant = createTestParticipant(3, testTrip, newUser, "participant");
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(savedParticipant);

        
        ParticipantResponseDTO result = participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getSudionikId()).isEqualTo(3);
        assertThat(result.getUloga()).isEqualTo("participant");
        assertThat(result.getUser().getEmail()).isEqualTo("newuser@example.com");

        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(tripRepository).findById(testTrip.getPutovanjeId());
        verify(userRepository).findByEmail("newuser@example.com");
        verify(participantRepository).findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), newUser.getKorisnikId());
        verify(participantRepository).save(any(Sudionik.class));
    }


    @Test
    void addParticipant_withNonOrganizerAccess_throwsException() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("newuser@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(false);

        
        assertThatThrownBy(() -> participantService.addParticipant(
                testTrip.getPutovanjeId(),
                participantUser.getKorisnikId(),
                addDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: Only organizers can add participants");

        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(tripRepository, never()).findById(any());
        verify(userRepository, never()).findByEmail(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void addParticipant_withNonExistentTrip_throwsException() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("newuser@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip not found");

        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(tripRepository).findById(testTrip.getPutovanjeId());
        verify(userRepository, never()).findByEmail(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void addParticipant_withNonExistentUser_throwsException() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("nonexistent@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");

        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(tripRepository).findById(testTrip.getPutovanjeId());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(participantRepository, never()).save(any());
    }

    @Test
    void addParticipant_withDuplicateParticipant_throwsException() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("participant@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("participant@example.com"))
                .thenReturn(Optional.of(participantUser));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(Optional.of(regularParticipant));

        
        assertThatThrownBy(() -> participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is already a participant of this trip");

        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(tripRepository).findById(testTrip.getPutovanjeId());
        verify(userRepository).findByEmail("participant@example.com");
        verify(participantRepository).findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void addParticipant_asOrganizer_addsOrganizerSuccessfully() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("newuser@example.com")
                .uloga("organizer")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(newUser));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), newUser.getKorisnikId()))
                .thenReturn(Optional.empty());

        Sudionik savedParticipant = createTestParticipant(3, testTrip, newUser, "organizer");
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(savedParticipant);

        
        ParticipantResponseDTO result = participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getUloga()).isEqualTo("organizer");
        assertThat(result.getUser().getEmail()).isEqualTo("newuser@example.com");

        verify(participantRepository).save(any(Sudionik.class));
    }


    

    @Test
    void listTripParticipants_withParticipantAccess_returnsParticipantList() {
        
        List<Sudionik> participants = Arrays.asList(organizerParticipant, regularParticipant);

        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.findByPutovanje_PutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(participants);

        
        List<ParticipantResponseDTO> result = participantService.listTripParticipants(
                testTrip.getPutovanjeId(),
                participantUser.getKorisnikId()
        );

        
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUloga()).isEqualTo("organizer");
        assertThat(result.get(1).getUloga()).isEqualTo("participant");

        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(participantRepository).findByPutovanje_PutovanjeId(testTrip.getPutovanjeId());
    }

    @Test
    void listTripParticipants_withNonParticipantAccess_throwsException() {
        
        Korisnik nonParticipant = createTestUser(4, "nonparticipant@example.com");

        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), nonParticipant.getKorisnikId()))
                .thenReturn(false);

        
        assertThatThrownBy(() -> participantService.listTripParticipants(
                testTrip.getPutovanjeId(),
                nonParticipant.getKorisnikId()
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");

        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), nonParticipant.getKorisnikId());
        verify(participantRepository, never()).findByPutovanje_PutovanjeId(any());
    }

    @Test
    void listTripParticipants_withEmptyParticipantList_returnsEmptyList() {
        
        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.findByPutovanje_PutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(Arrays.asList());

        
        List<ParticipantResponseDTO> result = participantService.listTripParticipants(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId()
        );

        
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripService).isUserParticipant(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).findByPutovanje_PutovanjeId(testTrip.getPutovanjeId());
    }

    

    @Test
    void updateParticipantRole_withOrganizerAccess_updatesRoleSuccessfully() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("organizer")
                .build();

        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(regularParticipant);

        
        ParticipantResponseDTO result = participantService.updateParticipantRole(
                regularParticipant.getSudionikId(),
                organizerUser.getKorisnikId(),
                updateDTO
        );

        
        assertThat(result).isNotNull();
        assertThat(result.getUloga()).isEqualTo("organizer");

        verify(participantRepository).findById(regularParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).save(any(Sudionik.class));
    }

    @Test
    void updateParticipantRole_withNonOrganizerAccess_throwsException() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("organizer")
                .build();

        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(false);

        
        assertThatThrownBy(() -> participantService.updateParticipantRole(
                regularParticipant.getSudionikId(),
                participantUser.getKorisnikId(),
                updateDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: Only organizers can update participant roles");

        verify(participantRepository).findById(regularParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void updateParticipantRole_withNonExistentParticipant_throwsException() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("organizer")
                .build();

        when(participantRepository.findById(999))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> participantService.updateParticipantRole(
                999,
                organizerUser.getKorisnikId(),
                updateDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Participant not found");

        verify(participantRepository).findById(999);
        verify(tripService, never()).isUserOrganizer(any(), any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void updateParticipantRole_demotingLastOrganizer_throwsException() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("participant")
                .build();

        when(participantRepository.findById(organizerParticipant.getSudionikId()))
                .thenReturn(Optional.of(organizerParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(1L);

        
        assertThatThrownBy(() -> participantService.updateParticipantRole(
                organizerParticipant.getSudionikId(),
                organizerUser.getKorisnikId(),
                updateDTO
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot demote the last organizer");

        verify(participantRepository).findById(organizerParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).countOrganizersByPutovanjeId(testTrip.getPutovanjeId());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void updateParticipantRole_demotingOrganizerWithMultipleOrganizers_updatesSuccessfully() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("participant")
                .build();

        when(participantRepository.findById(organizerParticipant.getSudionikId()))
                .thenReturn(Optional.of(organizerParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(2L);
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(organizerParticipant);

        
        ParticipantResponseDTO result = participantService.updateParticipantRole(
                organizerParticipant.getSudionikId(),
                organizerUser.getKorisnikId(),
                updateDTO
        );

        
        assertThat(result).isNotNull();

        verify(participantRepository).findById(organizerParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).countOrganizersByPutovanjeId(testTrip.getPutovanjeId());
        verify(participantRepository).save(any(Sudionik.class));
    }

    @Test
    void updateParticipantRole_promotingParticipantToOrganizer_updatesSuccessfully() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("organizer")
                .build();

        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(regularParticipant);

        
        ParticipantResponseDTO result = participantService.updateParticipantRole(
                regularParticipant.getSudionikId(),
                organizerUser.getKorisnikId(),
                updateDTO
        );

        
        assertThat(result).isNotNull();

        verify(participantRepository).findById(regularParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository, never()).countOrganizersByPutovanjeId(any());
        verify(participantRepository).save(any(Sudionik.class));
    }

    

    @Test
    void removeParticipant_withOrganizerAccess_removesParticipantSuccessfully() {
        
        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);

        
        participantService.removeParticipant(
                regularParticipant.getSudionikId(),
                organizerUser.getKorisnikId()
        );

        
        verify(participantRepository).findById(regularParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).delete(regularParticipant);
    }

    @Test
    void removeParticipant_withNonOrganizerAccess_throwsException() {
        
        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(false);

        
        assertThatThrownBy(() -> participantService.removeParticipant(
                regularParticipant.getSudionikId(),
                participantUser.getKorisnikId()
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: Only organizers can remove participants");

        verify(participantRepository).findById(regularParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(participantRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_withNonExistentParticipant_throwsException() {
        
        when(participantRepository.findById(999))
                .thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> participantService.removeParticipant(
                999,
                organizerUser.getKorisnikId()
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Participant not found");

        verify(participantRepository).findById(999);
        verify(tripService, never()).isUserOrganizer(any(), any());
        verify(participantRepository, never()).delete(any());
    }


    @Test
    void removeParticipant_removingLastOrganizer_throwsException() {
        
        when(participantRepository.findById(organizerParticipant.getSudionikId()))
                .thenReturn(Optional.of(organizerParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(1L);

        
        assertThatThrownBy(() -> participantService.removeParticipant(
                organizerParticipant.getSudionikId(),
                organizerUser.getKorisnikId()
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove the last organizer from the trip");

        verify(participantRepository).findById(organizerParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).countOrganizersByPutovanjeId(testTrip.getPutovanjeId());
        verify(participantRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_removingOrganizerWithMultipleOrganizers_removesSuccessfully() {
        
        when(participantRepository.findById(organizerParticipant.getSudionikId()))
                .thenReturn(Optional.of(organizerParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(2L);

        
        participantService.removeParticipant(
                organizerParticipant.getSudionikId(),
                organizerUser.getKorisnikId()
        );

        
        verify(participantRepository).findById(organizerParticipant.getSudionikId());
        verify(tripService).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository).countOrganizersByPutovanjeId(testTrip.getPutovanjeId());
        verify(participantRepository).delete(organizerParticipant);
    }

    

    @Test
    void addParticipant_verifiesAllRepositoryInteractions() {
        
        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("newuser@example.com")
                .uloga("participant")
                .build();

        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(tripRepository.findById(testTrip.getPutovanjeId()))
                .thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(newUser));
        when(participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), newUser.getKorisnikId()))
                .thenReturn(Optional.empty());

        Sudionik savedParticipant = createTestParticipant(3, testTrip, newUser, "participant");
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(savedParticipant);

        
        participantService.addParticipant(
                testTrip.getPutovanjeId(),
                organizerUser.getKorisnikId(),
                addDTO
        );

        
        verify(tripService, times(1)).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(tripRepository, times(1)).findById(testTrip.getPutovanjeId());
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(participantRepository, times(1)).findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                testTrip.getPutovanjeId(), newUser.getKorisnikId());
        verify(participantRepository, times(1)).save(any(Sudionik.class));
    }

    @Test
    void listTripParticipants_verifiesAllRepositoryInteractions() {
        
        List<Sudionik> participants = Arrays.asList(organizerParticipant, regularParticipant);

        when(tripService.isUserParticipant(testTrip.getPutovanjeId(), participantUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.findByPutovanje_PutovanjeId(testTrip.getPutovanjeId()))
                .thenReturn(participants);

        
        participantService.listTripParticipants(
                testTrip.getPutovanjeId(),
                participantUser.getKorisnikId()
        );

        
        verify(tripService, times(1)).isUserParticipant(testTrip.getPutovanjeId(), participantUser.getKorisnikId());
        verify(participantRepository, times(1)).findByPutovanje_PutovanjeId(testTrip.getPutovanjeId());
    }

    @Test
    void updateParticipantRole_verifiesAllRepositoryInteractions() {
        
        UpdateParticipantRoleDTO updateDTO = UpdateParticipantRoleDTO.builder()
                .uloga("organizer")
                .build();

        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);
        when(participantRepository.save(any(Sudionik.class)))
                .thenReturn(regularParticipant);

        
        participantService.updateParticipantRole(
                regularParticipant.getSudionikId(),
                organizerUser.getKorisnikId(),
                updateDTO
        );

        
        verify(participantRepository, times(1)).findById(regularParticipant.getSudionikId());
        verify(tripService, times(1)).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository, times(1)).save(any(Sudionik.class));
        verify(participantRepository, never()).countOrganizersByPutovanjeId(any());
    }

    @Test
    void removeParticipant_verifiesAllRepositoryInteractions() {
        
        when(participantRepository.findById(regularParticipant.getSudionikId()))
                .thenReturn(Optional.of(regularParticipant));
        when(tripService.isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId()))
                .thenReturn(true);

        
        participantService.removeParticipant(
                regularParticipant.getSudionikId(),
                organizerUser.getKorisnikId()
        );

        
        verify(participantRepository, times(1)).findById(regularParticipant.getSudionikId());
        verify(tripService, times(1)).isUserOrganizer(testTrip.getPutovanjeId(), organizerUser.getKorisnikId());
        verify(participantRepository, times(1)).delete(regularParticipant);
        verify(participantRepository, never()).countOrganizersByPutovanjeId(any());
    }
}
