package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.AuthService;
import com.tripplanner.domain.entity.Korisnik;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Initiates Google OAuth flow.
     */
    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> initiateGoogleAuth() {
        String authUrl = authService.initiateOAuthFlow("google");
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates Facebook OAuth flow.
     */
    @GetMapping("/facebook")
    public ResponseEntity<Map<String, String>> initiateFacebookAuth() {
        String authUrl = authService.initiateOAuthFlow("facebook");
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles OAuth callback (placeholder - requires OAuth configuration).
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleOAuthCallback(
            @RequestParam String code,
            @RequestParam String provider) {
        try {
            Korisnik user = authService.handleOAuthCallback(code, provider);
            String token = authService.generateSessionToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (UnsupportedOperationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(501).body(error);
        }
    }

    /**
     * Logout endpoint.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}
