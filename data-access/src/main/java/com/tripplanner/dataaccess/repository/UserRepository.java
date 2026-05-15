package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Korisnik} entities.
 * <p>
 * This repository provides CRUD operations and custom query methods
 * for user data access using Spring Data JPA.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<Korisnik, Integer> {

    /**
     * Finds a user by their email address.
     * <p>
     * Email addresses are unique in the system, so this method returns
     * at most one user.
     * </p>
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<Korisnik> findByEmail(String email);

    /**
     * Finds a user by their OAuth provider and OAuth ID.
     * <p>
     * This method is used to locate users who authenticate via OAuth providers
     * (e.g., Google, Facebook). The combination of provider and OAuth ID
     * uniquely identifies a user from an external authentication system.
     * </p>
     *
     * @param provider the OAuth provider name (e.g., "google", "facebook")
     * @param oauthId the unique identifier from the OAuth provider
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<Korisnik> findByOauthProviderAndOauthId(String provider, String oauthId);
}
