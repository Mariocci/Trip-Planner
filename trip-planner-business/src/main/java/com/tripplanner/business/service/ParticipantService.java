package com.tripplanner.business.service;

import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;

import java.util.List;

/**
 * Service interface for participant management operations.
 */
public interface ParticipantService {

    /**
     * Adds a participant to a trip. Only organizers can add participants.
     *
     * @param tripId the ID of the trip
     * @param organizerId the ID of the organizer adding the participant
     * @param addDTO the participant data
     * @return the created participant
     * @throws RuntimeException if organizer access denied or user not found
     */
    ParticipantResponseDTO addParticipant(Integer tripId, Integer organizerId, AddParticipantDTO addDTO);

    /**
     * Lists all participants for a trip.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the requesting user
     * @return list of participants
     * @throws RuntimeException if user is not a participant
     */
    List<ParticipantResponseDTO> listTripParticipants(Integer tripId, Integer userId);

    /**
     * Removes a participant from a trip. Only organizers can remove participants.
     * Cannot remove the last organizer.
     *
     * @param participantId the ID of the participant to remove
     * @param organizerId the ID of the organizer removing the participant
     * @throws RuntimeException if organizer access denied or last organizer removal attempted
     */
    void removeParticipant(Integer participantId, Integer organizerId);
}
