package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.LocationService;
import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    
    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(@Valid @RequestBody CreateLocationDTO createDTO) {
        LocationResponseDTO location = locationService.createLocation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getLocationById(@PathVariable Integer id) {
        LocationResponseDTO location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    
    @GetMapping("/search")
    public ResponseEntity<List<LocationResponseDTO>> searchLocations(@RequestParam String query) {
        List<LocationResponseDTO> locations = locationService.searchLocations(query);
        return ResponseEntity.ok(locations);
    }
}
