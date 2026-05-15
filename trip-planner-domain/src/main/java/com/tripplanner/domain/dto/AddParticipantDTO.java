package com.tripplanner.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddParticipantDTO {
    
    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email mora biti validan")
    private String email;
    
    @NotBlank(message = "Uloga je obavezna")
    private String uloga;
}
