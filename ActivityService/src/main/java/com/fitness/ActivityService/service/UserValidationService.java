package com.fitness.ActivityService.service;

import com.fitness.ActivityService.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;
    public boolean validateUser(String id){
        try{
        return userServiceWebClient.get().uri("/api/users/{id}/validate",id).retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
        catch(Exception e){
        e.printStackTrace();
        return false;}
    }
}
