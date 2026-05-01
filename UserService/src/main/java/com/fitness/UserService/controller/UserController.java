package com.fitness.UserService.controller;

import com.fitness.UserService.dto.RegisterUser;
import com.fitness.UserService.dto.ResponseUser;
import com.fitness.UserService.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
@GetMapping("/{id}")
    public ResponseEntity<ResponseUser> getUserProfile(@PathVariable String id) {
    return ResponseEntity.ok(userService.getUserProfile(id));

    }
    @PostMapping("/register")
    public ResponseEntity<ResponseUser> register(@Valid @RequestBody RegisterUser request){
        return ResponseEntity.ok(userService.register(request));
    }
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String id){
    return ResponseEntity.ok(userService.validateUser(id));
    }

}
