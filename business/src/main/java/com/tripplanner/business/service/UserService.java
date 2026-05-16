package com.tripplanner.business.service;

import com.tripplanner.domain.dto.UpdateUserDTO;
import com.tripplanner.domain.dto.UserResponseDTO;

/**
 * Service interface for user profile management operations.
 */
public interface UserService {

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user
     * @return the user details
     * @throws RuntimeException if user not found
     */
    UserResponseDTO getUserById(Integer userId);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address
     * @return the user details
     * @throws RuntimeException if user not found
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Updates a user's profile information.
     *
     * @param userId the ID of the user to update
     * @param updateDTO the update data
     * @return the updated user
     * @throws RuntimeException if user not found
     */
    UserResponseDTO updateUserProfile(Integer userId, UpdateUserDTO updateDTO);

    /**
     * Validates if an email address is unique in the system.
     *
     * @param email the email to check
     * @param excludeUserId optional user ID to exclude from the check
     * @return true if email is unique, false otherwise
     */
    boolean isEmailUnique(String email, Integer excludeUserId);

    /**
     * Finds an existing user by email or creates a new user from Auth0 data.
     * This method is called when a user logs in via Auth0 to ensure they exist in the database.
     *
     * @param email the user's email from Auth0
     * @param name the user's full name from Auth0
     * @param sub the Auth0 subject identifier (oauth_id)
     * @param picture the user's profile picture URL from Auth0
     * @return the user details (existing or newly created)
     */
    UserResponseDTO findOrCreateUserFromAuth0(String email, String name, String sub, String picture);
}
