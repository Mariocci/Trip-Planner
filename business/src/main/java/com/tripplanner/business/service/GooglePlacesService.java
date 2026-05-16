package com.tripplanner.business.service;

import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Google Places API.
 */
public interface GooglePlacesService {
    
    /**
     * Search for places using text query.
     * 
     * @param query The search query (e.g., "Zagreb, Croatia")
     * @return List of place suggestions with name, address, and coordinates
     */
    List<Map<String, Object>> searchPlaces(String query);
}
