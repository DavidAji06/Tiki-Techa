package com.tikitecha.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitecha.backend.dto.RegisterRequest;
import com.tikitecha.backend.model.User;
import com.tikitecha.backend.repository.UserRepository;
import com.tikitecha.backend.dto.LoginRequest;
import com.tikitecha.backend.dto.AuthResponse;
import com.tikitecha.backend.security.JwtService;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    // constructor injection — Spring automatically supplies these beans
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("Username already taken");
        }

        String hash = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), hash);
        userRepository.save(user);

        return ResponseEntity.status(201).body("User registered successfully");
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    return userRepository.findByUsername(request.getUsername())
            .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            .<ResponseEntity<?>>map(user -> ResponseEntity.ok(new AuthResponse(jwtService.generateToken(user.getUsername()))))
            .orElse(ResponseEntity.status(401).body("Invalid username or password"));
    }
}