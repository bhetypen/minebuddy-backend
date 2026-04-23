package com.minebuddy.controller;

import com.minebuddy.dto.response.UserResponseDTO;
import com.minebuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> listAll() {
        return userService.listAll();
    }

    @GetMapping("/pending")
    public List<UserResponseDTO> listPending() {
        return userService.listPending();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable UUID id) {
        try {
            UserResponseDTO user = userService.activate(id);
            return ResponseEntity.ok(Map.of(
                    "message", "User activated",
                    "user", user
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}