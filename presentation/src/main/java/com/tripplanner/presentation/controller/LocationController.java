package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.LocationService;
import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for location management operations.
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Creates a new location.
     *
     * @param createDTO the location creation data
     * @return the created location
     */
    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(@Valid @RequestBody CreateLocationDTO createDTO) {
        LocationResponseDTO location = locationService.createLocation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    /**
     * Retrieves a location by ID.
     *
     * @param id the location ID
     * @return the location details
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getLocationById(@PathVariable Integer id) {
        LocationResponseDTO location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    /**
     * Searches for locations.
     *
     * @param query the search query
     * @return list of matching locations
     */
    @GetMapping("/search")
    public ResponseEntity<List<LocationResponseDTO>> searchLocations(@RequestParam String query) {
        List<LocationResponseDTO> locations = locationService.searchLocations(query);
        return ResponseEntity.ok(locations);
    }
}
