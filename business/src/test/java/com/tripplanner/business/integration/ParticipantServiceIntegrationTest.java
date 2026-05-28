package com.tripplanner.business.integration;

import com.tripplanner.business.TestBusinessApplication;
import com.tripplanner.business.service.ParticipantService;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(classes = TestBusinessApplication.class)
@Transactional
@DisplayName("ParticipantService Integration Tests")
@Tag("integration")
class ParticipantServiceIntegrationTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Korisnik organizerUser;
    private Putovanje testTrip;
    private Sudionik organizerParticipant;

    @BeforeEach
    void setUp() {
        
        organizerUser = userRepository.save(Korisnik.builder()
                .ime("Solo")
                .prezime("Organizer")
                .email("solo.organizer@example.com")
                .oauthProvider("google")
                .oauthId("google-solo-organizer")
                .build());

        
        testTrip = tripRepository.save(Putovanje.builder()
                .naziv("Solo Organizer Trip")
                .opis("Trip used for last-organizer protection integration tests")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        
        organizerParticipant = participantRepository.save(Sudionik.builder()
                .uloga("organizer")
                .putovanje(testTrip)
                .korisnik(organizerUser)
                .build());

        
        entityManager.flush();
    }

    

    @Test
    @DisplayName("removeParticipant rejects removal of the last organizer with descriptive exception")
    void removeParticipant_lastOrganizer_throwsAndDoesNotDelete() {
        
        assertThat(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .isEqualTo(1L);

        
        assertThatThrownBy(() -> participantService.removeParticipant(
                organizerParticipant.getSudionikId(), organizerUser.getKorisnikId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove the last organizer");

        
        entityManager.flush();
        entityManager.clear();

        
        Optional<Sudionik> persisted =
                participantRepository.findById(organizerParticipant.getSudionikId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getUloga()).isEqualTo("organizer");
        assertThat(persisted.get().getKorisnik().getKorisnikId())
                .isEqualTo(organizerUser.getKorisnikId());
        assertThat(persisted.get().getPutovanje().getPutovanjeId())
                .isEqualTo(testTrip.getPutovanjeId());
    }

    @Test
    @DisplayName("removeParticipant on last organizer leaves organizer count unchanged")
    void removeParticipant_lastOrganizer_organizerCountUnchanged() {
        
        assertThatThrownBy(() -> participantService.removeParticipant(
                organizerParticipant.getSudionikId(), organizerUser.getKorisnikId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove the last organizer");

        entityManager.flush();
        entityManager.clear();

        
        Long organizerCount =
                participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId());
        assertThat(organizerCount).isEqualTo(1L);

        
        List<Sudionik> participants =
                participantRepository.findByPutovanje_PutovanjeId(testTrip.getPutovanjeId());
        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getUloga()).isEqualTo("organizer");
    }

    @Test
    @DisplayName("updateParticipantRole rejects demoting the last organizer and preserves role")
    void updateParticipantRole_demoteLastOrganizer_throwsAndPreservesOrganizerRole() {
        
        UpdateParticipantRoleDTO demoteDTO = UpdateParticipantRoleDTO.builder()
                .uloga("participant")
                .build();

        
        assertThatThrownBy(() -> participantService.updateParticipantRole(
                organizerParticipant.getSudionikId(), organizerUser.getKorisnikId(), demoteDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot demote the last organizer");

        entityManager.flush();
        entityManager.clear();

        
        Sudionik persisted = participantRepository.findById(organizerParticipant.getSudionikId())
                .orElseThrow();
        assertThat(persisted.getUloga()).isEqualTo("organizer");
        assertThat(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .isEqualTo(1L);
    }

    

    @Test
    @DisplayName("removeParticipant succeeds for an organizer when more than one organizer exists")
    void removeParticipant_organizerWithMultipleOrganizers_removesSuccessfully() {
        
        Korisnik secondOrganizerUser = userRepository.save(Korisnik.builder()
                .ime("Second")
                .prezime("Organizer")
                .email("second.organizer@example.com")
                .oauthProvider("google")
                .oauthId("google-second-organizer")
                .build());
        entityManager.flush();

        AddParticipantDTO addDTO = AddParticipantDTO.builder()
                .email("second.organizer@example.com")
                .uloga("organizer")
                .build();

        ParticipantResponseDTO secondOrganizer = participantService.addParticipant(
                testTrip.getPutovanjeId(), organizerUser.getKorisnikId(), addDTO);

        entityManager.flush();
        assertThat(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .isEqualTo(2L);

        
        participantService.removeParticipant(
                organizerParticipant.getSudionikId(), secondOrganizerUser.getKorisnikId());

        entityManager.flush();
        entityManager.clear();

        
        assertThat(participantRepository.findById(organizerParticipant.getSudionikId()))
                .isEmpty();
        assertThat(participantRepository.findById(secondOrganizer.getSudionikId()))
                .isPresent();
        assertThat(participantRepository.countOrganizersByPutovanjeId(testTrip.getPutovanjeId()))
                .isEqualTo(1L);
    }
}
