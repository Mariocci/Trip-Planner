package com.tripplanner.business.service;

import com.tripplanner.domain.dto.UpdateUserDTO;
import com.tripplanner.domain.dto.UserResponseDTO;


public interface UserService {

    
    UserResponseDTO getUserById(Integer userId);

    
    UserResponseDTO getUserByEmail(String email);

    
    UserResponseDTO updateUserProfile(Integer userId, UpdateUserDTO updateDTO);

    
    boolean isEmailUnique(String email, Integer excludeUserId);

    
    UserResponseDTO findOrCreateUserFromAuth0(String email, String name, String sub, String picture);
}
