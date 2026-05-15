package com.tripplanner.domain.dto;

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
public class ExpenseResponseDTO {
    
    private Integer trosakId;
    
    private BigDecimal iznos;
    
    private String opis;
    
    private LocalDate datum;
    
    private Integer putovanjeId;
}
