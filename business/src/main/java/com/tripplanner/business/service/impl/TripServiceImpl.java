package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateTripDTO;
import com.tripplanner.domain.dto.TripResponseDTO;
import com.tripplanner.domain.dto.UpdateTripDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    public TripServiceImpl(TripRepository tripRepository,
                          UserRepository userRepository,
                          ParticipantRepository participantRepository,
                          ExpenseRepository expenseRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public TripResponseDTO createTrip(Integer userId, CreateTripDTO createDTO) {
        
        if (createDTO.getDatumKraj().isBefore(createDTO.getDatumPoc())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        
        Korisnik user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        
        Putovanje trip = Putovanje.builder()
                .naziv(createDTO.getNaziv())
                .opis(createDTO.getOpis())
                .datumPoc(createDTO.getDatumPoc())
                .datumKraj(createDTO.getDatumKraj())
                .ukTrosak(BigDecimal.ZERO)
                .maxBudget(createDTO.getMaxBudget())
                .build();

        trip = tripRepository.save(trip);

        
        Sudionik organizer = Sudionik.builder()
                .putovanje(trip)
                .korisnik(user)
                .uloga("organizer")
                .build();
        participantRepository.save(organizer);

        return mapToResponseDTO(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO getTripById(Integer tripId, Integer userId) {
        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        return mapToResponseDTO(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> listUserTrips(Integer userId) {
        List<Putovanje> trips = tripRepository.findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(userId);
        return trips.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TripResponseDTO updateTrip(Integer tripId, Integer userId, UpdateTripDTO updateDTO) {
        if (!isUserOrganizer(tripId, userId)) {
            throw new RuntimeException("Access denied: Only organizers can update trips");
        }

        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        
        if (updateDTO.getNaziv() != null) {
            trip.setNaziv(updateDTO.getNaziv());
        }
        if (updateDTO.getOpis() != null) {
            trip.setOpis(updateDTO.getOpis());
        }
        if (updateDTO.getDatumPoc() != null) {
            trip.setDatumPoc(updateDTO.getDatumPoc());
        }
        if (updateDTO.getDatumKraj() != null) {
            trip.setDatumKraj(updateDTO.getDatumKraj());
        }
        if (updateDTO.getMaxBudget() != null) {
            trip.setMaxBudget(updateDTO.getMaxBudget());
        }

        
        if (trip.getDatumKraj().isBefore(trip.getDatumPoc())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        trip = tripRepository.save(trip);
        return mapToResponseDTO(trip);
    }

    @Override
    public void deleteTrip(Integer tripId, Integer userId) {
        if (!isUserOrganizer(tripId, userId)) {
            throw new RuntimeException("Access denied: Only organizers can delete trips");
        }

        tripRepository.deleteById(tripId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserOrganizer(Integer tripId, Integer userId) {
        return participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(tripId, userId)
                .map(participant -> "organizer".equals(participant.getUloga()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserParticipant(Integer tripId, Integer userId) {
        return participantRepository.findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(tripId, userId)
                .isPresent();
    }

    @Override
    public void recalculateTotalExpense(Integer tripId) {
        BigDecimal total = expenseRepository.sumByPutovanjeId(tripId);
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        
        trip.setUkTrosak(total);
        tripRepository.save(trip);
    }

    private TripResponseDTO mapToResponseDTO(Putovanje trip) {
        int participantCount = participantRepository.findByPutovanje_PutovanjeId(trip.getPutovanjeId()).size();
        
        return TripResponseDTO.builder()
                .putovanjeId(trip.getPutovanjeId())
                .naziv(trip.getNaziv())
                .opis(trip.getOpis())
                .datumPoc(trip.getDatumPoc())
                .datumKraj(trip.getDatumKraj())
                .ukTrosak(trip.getUkTrosak())
                .maxBudget(trip.getMaxBudget())
                .participantCount(participantCount)
                .build();
    }
}
