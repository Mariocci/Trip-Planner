package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.ActivityService;
import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponseDTO> createActivity(
            @PathVariable Integer tripId,
            @RequestParam Integer userId,
            @Valid @RequestBody CreateActivityDTO createDTO) {
        ActivityResponseDTO activity = activityService.createActivity(tripId, userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDTO> getActivityById(
            @PathVariable Integer activityId,
            @RequestParam Integer userId) {
        ActivityResponseDTO activity = activityService.getActivityById(activityId, userId);
        return ResponseEntity.ok(activity);
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponseDTO>> listTripActivities(
            @PathVariable Integer tripId,
            @RequestParam Integer userId) {
        List<ActivityResponseDTO> activities = activityService.listTripActivities(tripId, userId);
        return ResponseEntity.ok(activities);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDTO> updateActivity(
            @PathVariable Integer activityId,
            @RequestParam Integer userId,
            @Valid @RequestBody UpdateActivityDTO updateDTO) {
        ActivityResponseDTO activity = activityService.updateActivity(activityId, userId, updateDTO);
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Integer activityId,
            @RequestParam Integer userId) {
        activityService.deleteActivity(activityId, userId);
        return ResponseEntity.noContent().build();
    }
}
