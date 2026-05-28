package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.GooglePlacesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/places")
public class PlacesController {

    private final GooglePlacesService googlePlacesService;

    public PlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPlaces(@RequestParam String query) {
        List<Map<String, Object>> results = googlePlacesService.searchPlaces(query);
        return ResponseEntity.ok(results);
    }
}
