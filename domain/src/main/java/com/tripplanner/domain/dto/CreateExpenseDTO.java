package com.tripplanner.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExpenseDTO {
    
    @NotNull(message = "Iznos je obavezan")
    @Positive(message = "Iznos mora biti pozitivan")
    private BigDecimal iznos;
    
    private String opis;
    
    @NotNull(message = "Datum je obavezan")
    private LocalDate datum;
}
