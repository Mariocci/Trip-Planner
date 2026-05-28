package com.tripplanner.business.service;

import java.util.List;
import java.util.Map;


public interface GooglePlacesService {
    
    
    List<Map<String, Object>> searchPlaces(String query);
}
