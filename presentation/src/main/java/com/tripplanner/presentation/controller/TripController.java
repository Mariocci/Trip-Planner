package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.TripService;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(
            @RequestParam Integer userId,
            @Valid @RequestBody CreateTripDTO createDTO) {
        TripResponseDTO trip = tripService.createTrip(userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponseDTO> getTripById(
            @PathVariable Integer tripId,
            @RequestParam Integer userId) {
        TripResponseDTO trip = tripService.getTripById(tripId, userId);
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    public ResponseEntity<List<TripResponseDTO>> listUserTrips(@RequestParam Integer userId) {
        List<TripResponseDTO> trips = tripService.listUserTrips(userId);
        return ResponseEntity.ok(trips);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable Integer tripId,
            @RequestParam Integer userId,
            @Valid @RequestBody UpdateTripDTO updateDTO) {
        TripResponseDTO trip = tripService.updateTrip(tripId, userId, updateDTO);
        return ResponseEntity.ok(trip);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Integer tripId,
            @RequestParam Integer userId) {
        tripService.deleteTrip(tripId, userId);
        return ResponseEntity.noContent().build();
    }
}
