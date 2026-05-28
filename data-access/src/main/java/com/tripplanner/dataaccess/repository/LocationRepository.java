package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Lokacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LocationRepository extends JpaRepository<Lokacija, Integer> {
}
