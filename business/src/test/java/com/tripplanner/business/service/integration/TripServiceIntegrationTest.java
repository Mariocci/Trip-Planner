package com.tripplanner.business.service.integration;

import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Integration tests for {@link TripService} using real repositories backed by
 * an H2 in-memory database.
 *
 * <p>Verifies that creating a trip through the service correctly persists the
 * trip in {@link TripRepository} and automatically adds the creating user as
 * an organizer in {@link ParticipantRepository}.</p>
 *
 * <p>Validates Requirements: 4.1, 4.2, 4.6, 4.9</p>
 */
@SpringBootTest
@Transactional
@Tag("integration")
class TripServiceIntegrationTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    private Korisnik organizerUser;
    private CreateTripDTO createTripDTO;

    @BeforeEach
    void setUp() {
        // Persist a real user that will create the trip
        organizerUser = userRepository.save(Korisnik.builder()
                .ime("Alice")
                .prezime("Organizer")
                .email("alice.organizer@example.com")
                .oauthProvider("google")
                .oauthId("google-alice-organizer")
                .build());

        createTripDTO = CreateTripDTO.builder()
                .naziv("Paris Adventure")
                .opis("Summer vacation in Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .build();
    }

    @Nested
    @DisplayName("Trip creation persistence")
    class TripCreationPersistence {

        @Test
        @DisplayName("createTrip persists trip in TripRepository with all fields populated")
        void createTrip_persistsTripInTripRepository() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then - response carries persisted state
            assertThat(created).isNotNull();
            assertThat(created.getPutovanjeId()).isNotNull();
            assertThat(created.getNaziv()).isEqualTo("Paris Adventure");
            assertThat(created.getOpis()).isEqualTo("Summer vacation in Paris");
            assertThat(created.getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 1));
            assertThat(created.getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 10));
            assertThat(created.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);

            // Then - trip really exists in TripRepository
            Optional<Putovanje> persistedTrip = tripRepository.findById(created.getPutovanjeId());
            assertThat(persistedTrip).isPresent();
            assertThat(persistedTrip.get().getNaziv()).isEqualTo("Paris Adventure");
            assertThat(persistedTrip.get().getOpis()).isEqualTo("Summer vacation in Paris");
            assertThat(persistedTrip.get().getDatumPoc()).isEqualTo(LocalDate.of(2024, 6, 1));
            assertThat(persistedTrip.get().getDatumKraj()).isEqualTo(LocalDate.of(2024, 6, 10));
            assertThat(persistedTrip.get().getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("createTrip generates a non-null trip ID")
        void createTrip_generatesNonNullTripId() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then
            assertThat(created.getPutovanjeId()).isNotNull();
            assertThat(tripRepository.existsById(created.getPutovanjeId())).isTrue();
        }
    }

    @Nested
    @DisplayName("Creator added as organizer")
    class CreatorAddedAsOrganizer {

        @Test
        @DisplayName("createTrip adds creator to ParticipantRepository with organizer role")
        void createTrip_addsCreatorAsOrganizerInParticipantRepository() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then - participant exists for the new trip
            List<Sudionik> participants = participantRepository
                    .findByPutovanje_PutovanjeId(created.getPutovanjeId());
            assertThat(participants).hasSize(1);

            Sudionik organizer = participants.get(0);
            assertThat(organizer.getUloga()).isEqualTo("organizer");
            assertThat(organizer.getKorisnik()).isNotNull();
            assertThat(organizer.getKorisnik().getKorisnikId()).isEqualTo(organizerUser.getKorisnikId());
            assertThat(organizer.getPutovanje()).isNotNull();
            assertThat(organizer.getPutovanje().getPutovanjeId()).isEqualTo(created.getPutovanjeId());
        }

        @Test
        @DisplayName("createTrip results in exactly one organizer for the new trip")
        void createTrip_resultsInExactlyOneOrganizer() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then
            Long organizerCount = participantRepository
                    .countOrganizersByPutovanjeId(created.getPutovanjeId());
            assertThat(organizerCount).isEqualTo(1L);
        }

        @Test
        @DisplayName("isUserOrganizer returns true for the creator after createTrip")
        void createTrip_creatorIsRecognizedAsOrganizer() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then
            assertThat(tripService.isUserOrganizer(created.getPutovanjeId(), organizerUser.getKorisnikId()))
                    .isTrue();
            assertThat(tripService.isUserParticipant(created.getPutovanjeId(), organizerUser.getKorisnikId()))
                    .isTrue();
        }

        @Test
        @DisplayName("createTrip links the organizer participant to the persisted trip and user")
        void createTrip_organizerParticipantLinksToPersistedTripAndUser() {
            // When
            TripResponseDTO created = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);

            // Then
            Optional<Sudionik> organizerParticipant = participantRepository
                    .findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(
                            created.getPutovanjeId(), organizerUser.getKorisnikId());

            assertThat(organizerParticipant).isPresent();
            assertThat(organizerParticipant.get().getSudionikId()).isNotNull();
            assertThat(organizerParticipant.get().getUloga()).isEqualTo("organizer");
            assertThat(organizerParticipant.get().getKorisnik().getEmail())
                    .isEqualTo("alice.organizer@example.com");
            assertThat(organizerParticipant.get().getPutovanje().getNaziv())
                    .isEqualTo("Paris Adventure");
        }
    }

    @Nested
    @DisplayName("Multiple trip creations")
    class MultipleTripCreations {

        @Test
        @DisplayName("each createTrip call persists a separate trip and organizer entry")
        void createMultipleTrips_eachPersistedIndependently() {
            // Given
            CreateTripDTO secondTrip = CreateTripDTO.builder()
                    .naziv("Rome Adventure")
                    .opis("Historical tour")
                    .datumPoc(LocalDate.of(2024, 8, 1))
                    .datumKraj(LocalDate.of(2024, 8, 5))
                    .build();

            // When
            TripResponseDTO trip1 = tripService.createTrip(organizerUser.getKorisnikId(), createTripDTO);
            TripResponseDTO trip2 = tripService.createTrip(organizerUser.getKorisnikId(), secondTrip);

            // Then - both trips are persisted with unique IDs
            assertThat(trip1.getPutovanjeId()).isNotEqualTo(trip2.getPutovanjeId());
            assertThat(tripRepository.findById(trip1.getPutovanjeId())).isPresent();
            assertThat(tripRepository.findById(trip2.getPutovanjeId())).isPresent();

            // Then - each trip has its own organizer participant
            assertThat(participantRepository.countOrganizersByPutovanjeId(trip1.getPutovanjeId()))
                    .isEqualTo(1L);
            assertThat(participantRepository.countOrganizersByPutovanjeId(trip2.getPutovanjeId()))
                    .isEqualTo(1L);
        }
    }
}
