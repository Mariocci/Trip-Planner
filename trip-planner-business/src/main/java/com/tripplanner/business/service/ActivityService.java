package com.tripplanner.business.service;

import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;

import java.util.List;

/**
 * Service interface for activity management operations.
 */
public interface ActivityService {

    /**
     * Creates a new activity within a trip.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the user creating the activity
     * @param createDTO the activity creation data
     * @return the created activity
     * @throws RuntimeException if user is not a participant
     * @throws IllegalArgumentException if end datetime is before start datetime
     */
    ActivityResponseDTO createActivity(Integer tripId, Integer userId, CreateActivityDTO createDTO);

    /**
     * Retrieves an activity by ID.
     *
     * @param activityId the ID of the activity
     * @param userId the ID of the requesting user
     * @return the activity details
     * @throws RuntimeException if activity not found or user is not a participant
     */
    ActivityResponseDTO getActivityById(Integer activityId, Integer userId);

    /**
     * Lists all activities for a trip, ordered by start datetime.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the requesting user
     * @return list of activities
     * @throws RuntimeException if user is not a participant
     */
    List<ActivityResponseDTO> listTripActivities(Integer tripId, Integer userId);

    /**
     * Updates an activity.
     *
     * @param activityId the ID of the activity to update
     * @param userId the ID of the requesting user
     * @param updateDTO the update data
     * @return the updated activity
     * @throws RuntimeException if user is not a participant
     * @throws IllegalArgumentException if end datetime is before start datetime
     */
    ActivityResponseDTO updateActivity(Integer activityId, Integer userId, UpdateActivityDTO updateDTO);

    /**
     * Deletes an activity.
     *
     * @param activityId the ID of the activity to delete
     * @param userId the ID of the requesting user
     * @throws RuntimeException if user is not a participant
     */
    void deleteActivity(Integer activityId, Integer userId);
}
