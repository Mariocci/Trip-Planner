package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<Korisnik, Integer> {

    
    Optional<Korisnik> findByEmail(String email);

    
    Optional<Korisnik> findByOauthProviderAndOauthId(String provider, String oauthId);
}
