package com.tripplanner.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponseDTO {
    
    private Integer lokacijaId;
    
    private String naziv;
    
    private String adresa;
    
    private String grad;
    
    private String drzava;
}
