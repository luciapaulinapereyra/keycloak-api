package com.api.keycloak.services;

import com.api.keycloak.dto.UserDTO;
import com.api.keycloak.dto.UserRequestDTO;
import com.api.keycloak.models.User;
import com.api.keycloak.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
public class UserService {


    @Autowired
    private KeycloakUserService keycloakUserService;

    @Autowired
    private UserRepository userRepository;

    Logger logger = LoggerFactory.getLogger(UserService.class);



    @Transactional
    public ResponseEntity<UserDTO> registerUser(UserRequestDTO request, String role) {
        try {
            UserDTO user = keycloakUserService.registerUserInKeycloak(request, role);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            User userToSave = new User();
            userToSave.setEmail(user.getEmail());
            userToSave.setName(user.getName());
            userToSave.setRole(user.getRole());
            userToSave.setKeycloakId(user.getKeycloakId());
           userRepository.save(userToSave);

            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception exception) {
            logger.error("Error during user registration and save", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
}
}
