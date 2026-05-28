package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.LocationService;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.domain.dto.CreateLocationDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import com.tripplanner.domain.entity.Lokacija;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public LocationResponseDTO createLocation(CreateLocationDTO createDTO) {
        Lokacija location = Lokacija.builder()
                .naziv(createDTO.getNaziv())
                .adresa(createDTO.getAdresa())
                .grad(createDTO.getGrad())
                .drzava(createDTO.getDrzava())
                .build();

        location = locationRepository.save(location);
        return mapToResponseDTO(location);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponseDTO getLocationById(Integer locationId) {
        Lokacija location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return mapToResponseDTO(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponseDTO> searchLocations(String query) {
        
        
        List<Lokacija> locations = locationRepository.findAll();
        return locations.stream()
                .filter(loc -> loc.getNaziv().toLowerCase().contains(query.toLowerCase()) ||
                              loc.getGrad().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private LocationResponseDTO mapToResponseDTO(Lokacija location) {
        return LocationResponseDTO.builder()
                .lokacijaId(location.getLokacijaId())
                .naziv(location.getNaziv())
                .adresa(location.getAdresa())
                .grad(location.getGrad())
                .drzava(location.getDrzava())
                .build();
    }
}
