package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;

import java.util.List;


public interface LocationService {

    
    LocationResponseDTO createLocation(CreateLocationDTO createDTO);

    
    LocationResponseDTO getLocationById(Integer locationId);

    
    List<LocationResponseDTO> searchLocations(String query);
}
