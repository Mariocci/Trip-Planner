package com.tripplanner.business.service;

import com.tripplanner.domain.entity.Korisnik;

/**
 * Service interface for authentication and authorization operations.
 * <p>
 * This service handles OAuth authentication flows, user management,
 * and JWT token generation/validation.
 * </p>
 */
public interface AuthService {

    /**
     * Initiates the OAuth authentication flow for a given provider.
     *
     * @param provider the OAuth provider name ("google" or "facebook")
     * @return the OAuth authorization URL to redirect the user to
     * @throws IllegalArgumentException if the provider is not supported
     */
    String initiateOAuthFlow(String provider);

    /**
     * Handles the OAuth callback after user authorization.
     * <p>
     * Exchanges the authorization code for an access token,
     * retrieves user information from the OAuth provider,
     * and creates or updates the user in the database.
     * </p>
     *
     * @param code the authorization code from the OAuth provider
     * @param provider the OAuth provider name
     * @return the authenticated user
     * @throws IllegalArgumentException if the provider is not supported
     * @throws RuntimeException if OAuth authentication fails
     */
    Korisnik handleOAuthCallback(String code, String provider);

    /**
     * Creates a new user or updates an existing user with OAuth data.
     *
     * @param email the user's email address
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param provider the OAuth provider name
     * @param oauthId the unique identifier from the OAuth provider
     * @return the created or updated user
     */
    Korisnik createOrUpdateUser(String email, String firstName, String lastName, 
                                String provider, String oauthId);

    /**
     * Generates a JWT session token for an authenticated user.
     *
     * @param user the authenticated user
     * @return the JWT token string
     */
    String generateSessionToken(Korisnik user);

    /**
     * Validates a JWT session token and returns the associated user.
     *
     * @param token the JWT token to validate
     * @return the user associated with the token
     * @throws RuntimeException if the token is invalid or expired
     */
    Korisnik validateSessionToken(String token);
}
