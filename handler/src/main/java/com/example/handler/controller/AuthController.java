package com.example.handler.controller;

import com.example.handler.model.dto.LoginRequest;
import com.example.handler.model.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for username: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            if (authentication.isAuthenticated()) {
                log.info("Login successful for username: {}", loginRequest.getUsername());
                return ResponseEntity.ok(new LoginResponse(
                    "Login successful",
                    loginRequest.getUsername(),
                    true
                ));
            } else {
                log.warn("Login failed for username: {}", loginRequest.getUsername());
                return ResponseEntity.status(401).body(new LoginResponse(
                    "Login failed",
                    loginRequest.getUsername(),
                    false
                ));
            }
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for username: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body(new LoginResponse(
                "Invalid username or password",
                loginRequest.getUsername(),
                false
            ));
        } catch (Exception e) {
            log.error("Login error for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(500).body(new LoginResponse(
                "Internal server error",
                loginRequest.getUsername(),
                false
            ));
        }
    }
}