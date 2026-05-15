package com.tripplanner.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateActivityDTO {
    
    @NotBlank(message = "Naziv je obavezan")
    private String naziv;
    
    private String opis;
    
    @NotNull(message = "Datum i vrijeme početka je obavezno")
    private LocalDateTime datumVrijemePoc;
    
    @NotNull(message = "Datum i vrijeme kraja je obavezno")
    private LocalDateTime datumVrijemeKraj;
    
    @NotNull(message = "Lokacija je obavezna")
    private Integer lokacijaId;
    
    private List<Integer> categoryIds;
}
