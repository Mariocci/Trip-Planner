package com.tripplanner.business.service;

import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;

import java.util.List;


public interface ParticipantService {

    
    ParticipantResponseDTO addParticipant(Integer tripId, Integer organizerId, AddParticipantDTO addDTO);

    
    List<ParticipantResponseDTO> listTripParticipants(Integer tripId, Integer userId);

    
    ParticipantResponseDTO updateParticipantRole(Integer participantId, Integer organizerId, UpdateParticipantRoleDTO updateDTO);

    
    void removeParticipant(Integer participantId, Integer organizerId);
}
