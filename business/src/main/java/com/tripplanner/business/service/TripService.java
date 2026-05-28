package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.domain.entity.Putovanje;

import java.util.List;


public interface TripService {

    
    TripResponseDTO createTrip(Integer userId, CreateTripDTO createDTO);

    
    TripResponseDTO getTripById(Integer tripId, Integer userId);

    
    List<TripResponseDTO> listUserTrips(Integer userId);

    
    TripResponseDTO updateTrip(Integer tripId, Integer userId, UpdateTripDTO updateDTO);

    
    void deleteTrip(Integer tripId, Integer userId);

    
    boolean isUserOrganizer(Integer tripId, Integer userId);

    
    boolean isUserParticipant(Integer tripId, Integer userId);

    
    void recalculateTotalExpense(Integer tripId);
}
