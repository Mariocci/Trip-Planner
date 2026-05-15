package com.tripplanner.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTripDTO {
    
    private String naziv;
    
    private String opis;
    
    private LocalDate datumPoc;
    
    private LocalDate datumKraj;
}
