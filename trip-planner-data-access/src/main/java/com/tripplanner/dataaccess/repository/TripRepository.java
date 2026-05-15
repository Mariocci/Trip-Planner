package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Putovanje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link Putovanje} entities.
 * <p>
 * This repository provides CRUD operations and custom query methods
 * for trip data access using Spring Data JPA.
 * </p>
 */
@Repository
public interface TripRepository extends JpaRepository<Putovanje, Integer> {

    /**
     * Finds all trips for a specific user, ordered by start date descending.
     * <p>
     * This method retrieves trips where the user is a participant,
     * returning the most recent trips first.
     * </p>
     *
     * @param korisnikId the ID of the user
     * @return a list of trips for the user, ordered by start date (newest first)
     */
    List<Putovanje> findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(Integer korisnikId);
}
