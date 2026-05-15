package com.tripplanner.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTripDTO {
    
    @NotBlank(message = "Naziv je obavezan")
    private String naziv;
    
    private String opis;
    
    @NotNull(message = "Datum početka je obavezan")
    private LocalDate datumPoc;
    
    @NotNull(message = "Datum kraja je obavezan")
    private LocalDate datumKraj;
}
