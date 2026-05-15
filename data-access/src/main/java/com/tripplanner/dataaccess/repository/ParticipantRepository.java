package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Sudionik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Sudionik} entities.
 * <p>
 * This repository provides CRUD operations and custom query methods
 * for participant data access using Spring Data JPA.
 * </p>
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Sudionik, Integer> {

    /**
     * Finds all participants for a specific trip.
     * <p>
     * This method retrieves all participants associated with a trip.
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @return a list of participants for the trip
     */
    List<Sudionik> findByPutovanje_PutovanjeId(Integer putovanjeId);

    /**
     * Finds a specific participant by trip ID and user ID.
     * <p>
     * This method is used to check if a user is a participant in a trip
     * and to retrieve their participant details (e.g., role).
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @param korisnikId the ID of the user
     * @return an Optional containing the participant if found, or empty if not found
     */
    Optional<Sudionik> findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(Integer putovanjeId, Integer korisnikId);

    /**
     * Counts the number of organizers for a specific trip.
     * <p>
     * This method is used to ensure business rules around trip organizers,
     * such as requiring at least one organizer per trip.
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @return the count of organizers for the trip
     */
    @Query("SELECT COUNT(s) FROM Sudionik s WHERE s.putovanje.putovanjeId = :putovanjeId AND s.uloga = 'organizer'")
    Long countOrganizersByPutovanjeId(@Param("putovanjeId") Integer putovanjeId);
}
