package com.tripplanner.business.service;

import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;

import java.util.List;


public interface ActivityService {

    
    ActivityResponseDTO createActivity(Integer tripId, Integer userId, CreateActivityDTO createDTO);

    
    ActivityResponseDTO getActivityById(Integer activityId, Integer userId);

    
    List<ActivityResponseDTO> listTripActivities(Integer tripId, Integer userId);

    
    ActivityResponseDTO updateActivity(Integer activityId, Integer userId, UpdateActivityDTO updateDTO);

    
    void deleteActivity(Integer activityId, Integer userId);
}
