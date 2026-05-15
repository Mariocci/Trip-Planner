package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;

import java.util.List;

/**
 * Service interface for location management operations.
 */
public interface LocationService {

    /**
     * Creates a new location.
     *
     * @param createDTO the location creation data
     * @return the created location
     */
    LocationResponseDTO createLocation(CreateLocationDTO createDTO);

    /**
     * Retrieves a location by ID.
     *
     * @param locationId the ID of the location
     * @return the location details
     * @throws RuntimeException if location not found
     */
    LocationResponseDTO getLocationById(Integer locationId);

    /**
     * Searches for locations by query string.
     * In a full implementation, this would integrate with Google Maps API.
     *
     * @param query the search query
     * @return list of matching locations
     */
    List<LocationResponseDTO> searchLocations(String query);
}
