package com.tripplanner.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    
    private Integer korisnikId;
    
    private String ime;
    
    private String prezime;
    
    private String email;
    
    private String oauthProvider;
}
