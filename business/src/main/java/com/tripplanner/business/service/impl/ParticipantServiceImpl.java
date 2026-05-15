package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.ParticipantService;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;
import com.tripplanner.domain.dto.UserResponseDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ParticipantService}.
 */
@Service
@Transactional
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripService tripService;

    public ParticipantServiceImpl(ParticipantRepository participantRepository,
                                 TripRepository tripRepository,
                                 UserRepository userRepository,
                                 TripService tripService) {
        this.participantRepository = participantRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.tripService = tripService;
    }

    @Override
    public ParticipantResponseDTO addParticipant(Integer tripId, Integer organizerId, AddParticipantDTO addDTO) {
        if (!tripService.isUserOrganizer(tripId, organizerId)) {
            throw new RuntimeException("Access denied: Only organizers can add participants");
        }

        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Korisnik user = userRepository.findByEmail(addDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + addDTO.getEmail()));

        // Check if user is already a participant
        if (participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(tripId, user.getKorisnikId()).isPresent()) {
            throw new RuntimeException("User is already a participant of this trip");
        }

        Sudionik participant = Sudionik.builder()
                .putovanje(trip)
                .korisnik(user)
                .uloga(addDTO.getUloga())
                .build();

        participant = participantRepository.save(participant);
        return mapToResponseDTO(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponseDTO> listTripParticipants(Integer tripId, Integer userId) {
        if (!tripService.isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        List<Sudionik> participants = participantRepository.findByPutovanje_PutovanjeId(tripId);
        return participants.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipantResponseDTO updateParticipantRole(Integer participantId, Integer organizerId, UpdateParticipantRoleDTO updateDTO) {
        Sudionik participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Integer tripId = participant.getPutovanje().getPutovanjeId();

        if (!tripService.isUserOrganizer(tripId, organizerId)) {
            throw new RuntimeException("Access denied: Only organizers can update participant roles");
        }

        // Check if trying to demote the last organizer
        if ("organizer".equals(participant.getUloga()) && !"organizer".equalsIgnoreCase(updateDTO.getUloga())) {
            Long organizerCount = participantRepository.countOrganizersByPutovanjeId(tripId);
            if (organizerCount <= 1) {
                throw new RuntimeException("Cannot demote the last organizer");
            }
        }

        participant.setUloga(updateDTO.getUloga());
        participant = participantRepository.save(participant);
        return mapToResponseDTO(participant);
    }

    @Override
    public void removeParticipant(Integer participantId, Integer organizerId) {
        Sudionik participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Integer tripId = participant.getPutovanje().getPutovanjeId();

        if (!tripService.isUserOrganizer(tripId, organizerId)) {
            throw new RuntimeException("Access denied: Only organizers can remove participants");
        }

        // Check if this is the last organizer
        if ("organizer".equals(participant.getUloga())) {
            Long organizerCount = participantRepository.countOrganizersByPutovanjeId(tripId);
            if (organizerCount <= 1) {
                throw new RuntimeException("Cannot remove the last organizer from the trip");
            }
        }

        participantRepository.delete(participant);
    }

    private ParticipantResponseDTO mapToResponseDTO(Sudionik participant) {
        UserResponseDTO userDTO = UserResponseDTO.builder()
                .korisnikId(participant.getKorisnik().getKorisnikId())
                .ime(participant.getKorisnik().getIme())
                .prezime(participant.getKorisnik().getPrezime())
                .email(participant.getKorisnik().getEmail())
                .oauthProvider(participant.getKorisnik().getOauthProvider())
                .build();

        return ParticipantResponseDTO.builder()
                .sudionikId(participant.getSudionikId())
                .uloga(participant.getUloga())
                .user(userDTO)
                .build();
    }
}
