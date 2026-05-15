package com.tripplanner.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateParticipantRoleDTO {
    
    @NotBlank(message = "Uloga je obavezna")
    private String uloga;
}
