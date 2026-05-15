package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.ParticipantService;
import com.tripplanner.domain.dto.AddParticipantDTO;
import com.tripplanner.domain.dto.ParticipantResponseDTO;
import com.tripplanner.domain.dto.UpdateParticipantRoleDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<ParticipantResponseDTO> addParticipant(
            @PathVariable Integer tripId,
            @RequestParam Integer requestingUserId,
            @Valid @RequestBody AddParticipantDTO addDTO) {
        ParticipantResponseDTO participant = participantService.addParticipant(tripId, requestingUserId, addDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(participant);
    }

    @GetMapping
    public ResponseEntity<List<ParticipantResponseDTO>> listTripParticipants(
            @PathVariable Integer tripId,
            @RequestParam Integer userId) {
        List<ParticipantResponseDTO> participants = participantService.listTripParticipants(tripId, userId);
        return ResponseEntity.ok(participants);
    }

    @PutMapping("/{participantId}/role")
    public ResponseEntity<ParticipantResponseDTO> updateParticipantRole(
            @PathVariable Integer tripId,
            @PathVariable Integer participantId,
            @RequestParam Integer requestingUserId,
            @Valid @RequestBody UpdateParticipantRoleDTO updateDTO) {
        ParticipantResponseDTO participant = participantService.updateParticipantRole(
                participantId, requestingUserId, updateDTO);
        return ResponseEntity.ok(participant);
    }

    @DeleteMapping("/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Integer tripId,
            @PathVariable Integer participantId,
            @RequestParam Integer requestingUserId) {
        participantService.removeParticipant(participantId, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}
