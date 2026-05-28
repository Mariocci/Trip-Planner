package com.tripplanner.business.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripplanner.business.service.GooglePlacesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GooglePlacesServiceImpl implements GooglePlacesService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> searchPlaces(String query) {
        String url = "https://places.googleapis.com/v1/places:searchText";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.location,places.types");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", query);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            List<Map<String, Object>> results = parsePlacesResponse(response.getBody());
            
            
            
            results.sort((a, b) -> {
                boolean aIsCity = isCity(a);
                boolean bIsCity = isCity(b);
                if (aIsCity && !bIsCity) return -1;
                if (!aIsCity && bIsCity) return 1;
                return 0;
            });
            
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search places: " + e.getMessage(), e);
        }
    }
    
    private boolean isCity(Map<String, Object> place) {
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) place.get("types");
        if (types == null) return false;
        return types.contains("locality") || types.contains("administrative_area_level_1") || 
               types.contains("administrative_area_level_2") || types.contains("country");
    }
    
    private boolean isUnwantedPlaceType(Map<String, Object> place) {
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) place.get("types");
        if (types == null) return false;
        
        
        List<String> unwantedTypes = java.util.Arrays.asList(
            "lodging", "real_estate_agency", "apartment_building"
        );
        
        for (String unwantedType : unwantedTypes) {
            if (types.contains(unwantedType)) {
                return true;
            }
        }
        return false;
    }
    
    private List<Map<String, Object>> parsePlacesResponse(String responseBody) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode places = root.get("places");
            
            if (places != null && places.isArray()) {
                for (JsonNode place : places) {
                    Map<String, Object> placeData = new HashMap<>();
                    
                    JsonNode displayName = place.get("displayName");
                    if (displayName != null && displayName.has("text")) {
                        placeData.put("name", displayName.get("text").asText());
                    }
                    
                    JsonNode formattedAddress = place.get("formattedAddress");
                    if (formattedAddress != null) {
                        placeData.put("address", formattedAddress.asText());
                    }
                    
                    JsonNode location = place.get("location");
                    if (location != null) {
                        placeData.put("latitude", location.get("latitude").asDouble());
                        placeData.put("longitude", location.get("longitude").asDouble());
                    }
                    
                    JsonNode types = place.get("types");
                    if (types != null && types.isArray()) {
                        List<String> typesList = new ArrayList<>();
                        for (JsonNode type : types) {
                            typesList.add(type.asText());
                        }
                        placeData.put("types", typesList);
                    }
                    
                    results.add(placeData);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse places response: " + e.getMessage(), e);
        }
        
        return results;
    }
}
