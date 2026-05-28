package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.UserService;
import com.tripplanner.domain.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.clientId}")
    private String auth0ClientId;

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getAuthConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("domain", auth0Domain);
        config.put("clientId", auth0ClientId);
        config.put("audience", "https://" + auth0Domain + "/api/v2/");
        config.put("redirectUri", "http://localhost:5173/callback");
        return ResponseEntity.ok(config);
    }

    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");
        String sub = jwt.getSubject();

        
        UserResponseDTO user = userService.findOrCreateUserFromAuth0(email, name, sub, picture);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("korisnikId", user.getKorisnikId());
        userInfo.put("sub", sub);
        userInfo.put("email", email);
        userInfo.put("name", name);
        userInfo.put("picture", picture);
        userInfo.put("email_verified", jwt.getClaimAsBoolean("email_verified"));
        
        return ResponseEntity.ok(userInfo);
    }

    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("logoutUrl", "https://" + auth0Domain + "/v2/logout?client_id=" + auth0ClientId + "&returnTo=http://localhost:5173");
        return ResponseEntity.ok(response);
    }
}
