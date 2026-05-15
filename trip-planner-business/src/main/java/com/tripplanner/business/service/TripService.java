package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.domain.entity.Putovanje;

import java.util.List;

/**
 * Service interface for trip management operations.
 */
public interface TripService {

    /**
     * Creates a new trip and automatically adds the creating user as an organizer.
     *
     * @param userId the ID of the user creating the trip
     * @param createDTO the trip creation data
     * @return the created trip
     * @throws IllegalArgumentException if end date is before start date
     */
    TripResponseDTO createTrip(Integer userId, CreateTripDTO createDTO);

    /**
     * Retrieves a trip by ID if the user is a participant.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the requesting user
     * @return the trip details
     * @throws RuntimeException if trip not found or user is not a participant
     */
    TripResponseDTO getTripById(Integer tripId, Integer userId);

    /**
     * Lists all trips where the user is a participant.
     *
     * @param userId the ID of the user
     * @return list of trips ordered by start date descending
     */
    List<TripResponseDTO> listUserTrips(Integer userId);

    /**
     * Updates a trip. Only organizers can update trips.
     *
     * @param tripId the ID of the trip to update
     * @param userId the ID of the requesting user
     * @param updateDTO the update data
     * @return the updated trip
     * @throws RuntimeException if user is not an organizer
     * @throws IllegalArgumentException if end date is before start date
     */
    TripResponseDTO updateTrip(Integer tripId, Integer userId, UpdateTripDTO updateDTO);

    /**
     * Deletes a trip. Only organizers can delete trips.
     * Cascades to all activities, expenses, and participants.
     *
     * @param tripId the ID of the trip to delete
     * @param userId the ID of the requesting user
     * @throws RuntimeException if user is not an organizer
     */
    void deleteTrip(Integer tripId, Integer userId);

    /**
     * Validates if a user is an organizer of a trip.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the user
     * @return true if user is an organizer, false otherwise
     */
    boolean isUserOrganizer(Integer tripId, Integer userId);

    /**
     * Validates if a user is a participant of a trip.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the user
     * @return true if user is a participant, false otherwise
     */
    boolean isUserParticipant(Integer tripId, Integer userId);

    /**
     * Recalculates the total expense for a trip based on all expenses.
     *
     * @param tripId the ID of the trip
     */
    void recalculateTotalExpense(Integer tripId);
}
