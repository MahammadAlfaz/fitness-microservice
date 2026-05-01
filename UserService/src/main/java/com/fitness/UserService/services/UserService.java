package com.fitness.UserService.services;

import com.fitness.UserService.dto.RegisterUser;
import com.fitness.UserService.dto.ResponseUser;
import com.fitness.UserService.model.User;
import com.fitness.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;




@RequiredArgsConstructor
@Service
public class UserService {
    private final  UserRepository userRepository;
    public  ResponseUser register(RegisterUser request) {
        if(userRepository.existsByEmail(request.getEmail())){
            User existingUser=userRepository.findByEmail(request.getEmail());
            ResponseUser responseUser = ResponseUser.builder()
                    .id(existingUser.getId())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt())
                    .build();

            return responseUser;
        }
        User user = User.builder()
                .email(request.getEmail())
                .keyCloakId(request.getKeyCloakId())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }
        public ResponseUser mapToResponse(User user){
        ResponseUser responseUser = ResponseUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .keyCloakId(user.getKeyCloakId())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return responseUser;
        }

    public  ResponseUser getUserProfile(String id) {
        return mapToResponse(userRepository.findById(id).orElseThrow(()->
                 new RuntimeException("User not found with id " + id)
        ));
    }

    public  Boolean validateUser(String id) {
        return userRepository.existsByKeyCloakId(id);
    }
}

