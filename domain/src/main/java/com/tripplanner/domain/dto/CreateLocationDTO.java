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
public class CreateLocationDTO {
    
    @NotBlank(message = "Naziv je obavezan")
    private String naziv;
    
    private String adresa;
    
    @NotBlank(message = "Grad je obavezan")
    private String grad;
    
    @NotBlank(message = "Država je obavezna")
    private String drzava;
}
