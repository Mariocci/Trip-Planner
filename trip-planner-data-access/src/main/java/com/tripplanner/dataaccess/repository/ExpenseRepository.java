package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Trosak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for managing {@link Trosak} entities.
 * <p>
 * This repository provides CRUD operations and custom query methods
 * for expense data access using Spring Data JPA.
 * </p>
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Trosak, Integer> {

    /**
     * Finds all expenses for a specific trip.
     * <p>
     * This method retrieves all expenses associated with a trip.
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @return a list of expenses for the trip
     */
    List<Trosak> findByPutovanje_PutovanjeId(Integer putovanjeId);

    /**
     * Calculates the total sum of expenses for a specific trip.
     * <p>
     * This method aggregates all expense amounts for a trip,
     * returning the total cost. Returns null if no expenses exist.
     * </p>
     *
     * @param putovanjeId the ID of the trip
     * @return the total sum of expenses, or null if no expenses exist
     */
    @Query("SELECT SUM(t.iznos) FROM Trosak t WHERE t.putovanje.putovanjeId = :putovanjeId")
    BigDecimal sumByPutovanjeId(@Param("putovanjeId") Integer putovanjeId);
}
