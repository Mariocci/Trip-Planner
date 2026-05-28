package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.UserService;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.UpdateUserDTO;
import com.tripplanner.domain.dto.UserResponseDTO;
import com.tripplanner.domain.entity.Korisnik;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer userId) {
        Korisnik user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        Korisnik user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUserProfile(Integer userId, UpdateUserDTO updateDTO) {
        Korisnik user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (updateDTO.getIme() != null) {
            user.setIme(updateDTO.getIme());
        }
        if (updateDTO.getPrezime() != null) {
            user.setPrezime(updateDTO.getPrezime());
        }

        user = userRepository.save(user);
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Integer excludeUserId) {
        return userRepository.findByEmail(email)
                .map(user -> user.getKorisnikId().equals(excludeUserId))
                .orElse(true);
    }

    @Override
    public UserResponseDTO findOrCreateUserFromAuth0(String email, String name, String sub, String picture) {
        return userRepository.findByEmail(email)
                .map(this::mapToResponseDTO)
                .orElseGet(() -> {
                    
                    String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
                    String firstName = nameParts.length > 0 ? nameParts[0] : "";
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";
                    
                    Korisnik newUser = Korisnik.builder()
                            .email(email)
                            .ime(firstName)
                            .prezime(lastName)
                            .oauthProvider("auth0")
                            .oauthId(sub)
                            .build();
                    newUser = userRepository.save(newUser);
                    return mapToResponseDTO(newUser);
                });
    }

    private UserResponseDTO mapToResponseDTO(Korisnik user) {
        return UserResponseDTO.builder()
                .korisnikId(user.getKorisnikId())
                .ime(user.getIme())
                .prezime(user.getPrezime())
                .email(user.getEmail())
                .oauthProvider(user.getOauthProvider())
                .build();
    }
}
