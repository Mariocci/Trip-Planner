package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Kategorija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Kategorija} entities.
 * <p>
 * This repository provides CRUD operations for category data access
 * using Spring Data JPA.
 * </p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Kategorija, Integer> {
}
