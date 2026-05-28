package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Kategorija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Kategorija, Integer> {
}
