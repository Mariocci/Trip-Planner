package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Aktivnost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link Aktivnost} entities.
 * <p>
 * This repository provides CRUD operations and custom query methods
 * for activity data access using Spring Data JPA.
 * </p>
 */
@Repository
public interface ActivityRepository extends JpaRepository<Aktivnost, Integer> {

    /**
     * Finds all activities for a specific trip, ordered by start date/time.
     * <p>
     * This method retrieves activities associated with a trip,
     * returning them in chronological order.
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @return a list of activities for the trip, ordered by start date/time
     */
    List<Aktivnost> findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(Integer putovanjeId);
}
