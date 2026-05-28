package com.tripplanner.business.service;

import com.tripplanner.domain.entity.Korisnik;


public interface AuthService {

    
    String initiateOAuthFlow(String provider);

    
    Korisnik handleOAuthCallback(String code, String provider);

    
    Korisnik createOrUpdateUser(String email, String firstName, String lastName, 
                                String provider, String oauthId);

    
    String generateSessionToken(Korisnik user);

    
    Korisnik validateSessionToken(String token);
}
