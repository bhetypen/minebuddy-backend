package com.minebuddy.service;

import com.minebuddy.dto.response.UserResponseDTO;
import com.minebuddy.model.User;
import com.minebuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listAll() {
        return userRepo.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listPending() {
        return userRepo.findByActiveFalse().stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public UserResponseDTO activate(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isActive()) {
            return toResponseDTO(user);
        }

        user.setActive(true);
        User saved = userRepo.save(user);
        return toResponseDTO(saved);
    }

    private UserResponseDTO toResponseDTO(User u) {
        return new UserResponseDTO(
                u.getUserId().toString(),
                u.getEmail(),
                u.getName(),
                u.isActive(),
                u.isSuperAdmin(),
                u.getCreatedAt()
        );
    }
}