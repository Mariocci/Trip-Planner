package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.ActivityService;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ActivityRepository;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.dataaccess.repository.LocationRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.domain.dto.ActivityResponseDTO;
import com.tripplanner.domain.dto.CategoryResponseDTO;
import com.tripplanner.domain.dto.CreateActivityDTO;
import com.tripplanner.domain.dto.LocationResponseDTO;
import com.tripplanner.domain.dto.UpdateActivityDTO;
import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ActivityService}.
 */
@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final TripService tripService;

    public ActivityServiceImpl(ActivityRepository activityRepository,
                              TripRepository tripRepository,
                              LocationRepository locationRepository,
                              CategoryRepository categoryRepository,
                              TripService tripService) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.tripService = tripService;
    }

    @Override
    public ActivityResponseDTO createActivity(Integer tripId, Integer userId, CreateActivityDTO createDTO) {
        if (!tripService.isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        if (createDTO.getDatumVrijemeKraj().isBefore(createDTO.getDatumVrijemePoc())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }

        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Lokacija location = locationRepository.findById(createDTO.getLokacijaId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        Aktivnost activity = Aktivnost.builder()
                .naziv(createDTO.getNaziv())
                .opis(createDTO.getOpis())
                .datumVrijemePoc(createDTO.getDatumVrijemePoc())
                .datumVrijemeKraj(createDTO.getDatumVrijemeKraj())
                .putovanje(trip)
                .lokacija(location)
                .categories(new ArrayList<>())
                .build();

        if (createDTO.getCategoryIds() != null && !createDTO.getCategoryIds().isEmpty()) {
            List<Kategorija> categories = categoryRepository.findAllById(createDTO.getCategoryIds());
            activity.setCategories(categories);
        }

        activity = activityRepository.save(activity);
        return mapToResponseDTO(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDTO getActivityById(Integer activityId, Integer userId) {
        Aktivnost activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!tripService.isUserParticipant(activity.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        return mapToResponseDTO(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listTripActivities(Integer tripId, Integer userId) {
        if (!tripService.isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        List<Aktivnost> activities = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(tripId);
        return activities.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityResponseDTO updateActivity(Integer activityId, Integer userId, UpdateActivityDTO updateDTO) {
        Aktivnost activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!tripService.isUserParticipant(activity.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        if (updateDTO.getNaziv() != null) {
            activity.setNaziv(updateDTO.getNaziv());
        }
        if (updateDTO.getOpis() != null) {
            activity.setOpis(updateDTO.getOpis());
        }
        if (updateDTO.getDatumVrijemePoc() != null) {
            activity.setDatumVrijemePoc(updateDTO.getDatumVrijemePoc());
        }
        if (updateDTO.getDatumVrijemeKraj() != null) {
            activity.setDatumVrijemeKraj(updateDTO.getDatumVrijemeKraj());
        }
        if (updateDTO.getLokacijaId() != null) {
            Lokacija location = locationRepository.findById(updateDTO.getLokacijaId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            activity.setLokacija(location);
        }
        if (updateDTO.getCategoryIds() != null) {
            List<Kategorija> categories = categoryRepository.findAllById(updateDTO.getCategoryIds());
            activity.setCategories(categories);
        }

        if (activity.getDatumVrijemeKraj().isBefore(activity.getDatumVrijemePoc())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }

        activity = activityRepository.save(activity);
        return mapToResponseDTO(activity);
    }

    @Override
    public void deleteActivity(Integer activityId, Integer userId) {
        Aktivnost activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!tripService.isUserParticipant(activity.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        activityRepository.delete(activity);
    }

    private ActivityResponseDTO mapToResponseDTO(Aktivnost activity) {
        LocationResponseDTO locationDTO = LocationResponseDTO.builder()
                .lokacijaId(activity.getLokacija().getLokacijaId())
                .naziv(activity.getLokacija().getNaziv())
                .adresa(activity.getLokacija().getAdresa())
                .grad(activity.getLokacija().getGrad())
                .drzava(activity.getLokacija().getDrzava())
                .build();

        List<CategoryResponseDTO> categoryDTOs = activity.getCategories().stream()
                .map(cat -> CategoryResponseDTO.builder()
                        .kategorijaId(cat.getKategorijaId())
                        .naziv(cat.getNaziv())
                        .opis(cat.getOpis())
                        .build())
                .collect(Collectors.toList());

        return ActivityResponseDTO.builder()
                .aktivnostId(activity.getAktivnostId())
                .naziv(activity.getNaziv())
                .opis(activity.getOpis())
                .datumVrijemePoc(activity.getDatumVrijemePoc())
                .datumVrijemeKraj(activity.getDatumVrijemeKraj())
                .location(locationDTO)
                .categories(categoryDTOs)
                .build();
    }
}
