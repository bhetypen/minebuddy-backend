package com.minebuddy.controller;

import com.minebuddy.dto.request.LoginRequestDTO;
import com.minebuddy.dto.request.RefreshRequestDTO;
import com.minebuddy.dto.request.RegisterRequestDTO;
import com.minebuddy.dto.response.LoginResponseDTO;
import com.minebuddy.dto.response.UserResponseDTO;
import com.minebuddy.service.AuthService;
import com.minebuddy.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        try {
            UserResponseDTO user = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration submitted. Waiting for admin approval.",
                    "user", user
            ));
        } catch (AuthService.AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req) {
        try {
            LoginResponseDTO response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (AuthService.AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDTO req) {
        try {
            LoginResponseDTO response = authService.refresh(req.refreshToken());
            return ResponseEntity.ok(response);
        } catch (RefreshTokenService.RefreshTokenException | AuthService.AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequestDTO req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}