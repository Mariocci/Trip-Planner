package com.tripplanner.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponseDTO {
    
    private Integer sudionikId;
    
    private String uloga;
    
    private UserResponseDTO user;
}
