package com.minebuddy.service;

import com.minebuddy.dto.request.LoginRequestDTO;
import com.minebuddy.dto.request.RegisterRequestDTO;
import com.minebuddy.dto.response.LoginResponseDTO;
import com.minebuddy.dto.response.UserResponseDTO;
import com.minebuddy.model.User;
import com.minebuddy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new AuthException("An account with this email already exists");
        }

        User user = new User(
                req.email().toLowerCase().trim(),
                passwordEncoder.encode(req.password()),
                req.name().trim()
        );
        User saved = userRepo.save(user);
        return toResponseDTO(saved);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO req) {
        User user = userRepo.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new AuthException("Account is pending approval by an administrator");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshTokenService.IssuedToken refresh = refreshTokenService.generateForUser(user.getUserId());

        return new LoginResponseDTO(
                accessToken,
                refresh.rawToken(),
                jwtService.getAccessTokenExpirySeconds(),
                user.getUserId().toString(),
                user.getEmail(),
                user.getName(),
                user.isSuperAdmin()
        );
    }

    @Transactional
    public LoginResponseDTO refresh(String refreshToken) {
        RefreshTokenService.IssuedToken newRefresh = refreshTokenService.rotate(refreshToken);

        User user = userRepo.findById(newRefresh.entity().getUserId())
                .orElseThrow(() -> new AuthException("User no longer exists"));

        if (!user.isActive()) {
            refreshTokenService.revokeAllForUser(user.getUserId());
            throw new AuthException("Account is no longer active");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResponseDTO(
                accessToken,
                newRefresh.rawToken(),
                jwtService.getAccessTokenExpirySeconds(),
                user.getUserId().toString(),
                user.getEmail(),
                user.getName(),
                user.isSuperAdmin()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
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

    public static class AuthException extends RuntimeException {
        public AuthException(String msg) { super(msg); }
    }
}