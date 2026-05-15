package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Lokacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Lokacija} entities.
 * <p>
 * This repository provides CRUD operations for location data access
 * using Spring Data JPA.
 * </p>
 */
@Repository
public interface LocationRepository extends JpaRepository<Lokacija, Integer> {
}
