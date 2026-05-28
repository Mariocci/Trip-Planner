package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Sudionik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ParticipantRepository extends JpaRepository<Sudionik, Integer> {

    
    List<Sudionik> findByPutovanje_PutovanjeId(Integer putovanjeId);

    
    Optional<Sudionik> findByPutovanje_PutovanjeIdAndKorisnik_KorisnikId(Integer putovanjeId, Integer korisnikId);

    
    @Query("SELECT COUNT(s) FROM Sudionik s WHERE s.putovanje.putovanjeId = :putovanjeId AND s.uloga = 'organizer'")
    Long countOrganizersByPutovanjeId(@Param("putovanjeId") Integer putovanjeId);
}
