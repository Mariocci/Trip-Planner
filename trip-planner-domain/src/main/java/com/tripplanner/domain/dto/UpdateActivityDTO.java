package com.tripplanner.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateActivityDTO {
    
    private String naziv;
    
    private String opis;
    
    private LocalDateTime datumVrijemePoc;
    
    private LocalDateTime datumVrijemeKraj;
    
    private Integer lokacijaId;
    
    private List<Integer> categoryIds;
}
