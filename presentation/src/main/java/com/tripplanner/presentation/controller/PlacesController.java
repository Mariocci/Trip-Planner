package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.GooglePlacesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Google Places API operations.
 */
@RestController
@RequestMapping("/api/places")
public class PlacesController {

    private final GooglePlacesService googlePlacesService;

    public PlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    /**
     * Search for places using text query.
     * 
     * @param query The search query
     * @return List of place suggestions
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPlaces(@RequestParam String query) {
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);
        return ResponseEntity.ok(results);
    }
}
