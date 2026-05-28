package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Aktivnost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ActivityRepository extends JpaRepository<Aktivnost, Integer> {

    
    List<Aktivnost> findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(Integer putovanjeId);
}
