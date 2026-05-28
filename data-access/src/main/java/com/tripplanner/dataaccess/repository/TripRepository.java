package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Putovanje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TripRepository extends JpaRepository<Putovanje, Integer> {

    
    List<Putovanje> findByParticipants_Korisnik_KorisnikIdOrderByDatumPocDesc(Integer korisnikId);
}
