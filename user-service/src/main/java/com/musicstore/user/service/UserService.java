package com.musicstore.user.service;

import com.musicstore.user.api.v1.dto.*;
import com.musicstore.user.domain.entity.Role;
import com.musicstore.user.domain.entity.User;
import com.musicstore.user.domain.repository.UserRepository;
import com.musicstore.user.security.JwtDenylistService;
import com.musicstore.user.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtDenylistService jwtDenylistService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Email already registered: " + request.email()
            );
        }

        User user = User.builder()
            .email(request.email().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .role(Role.CUSTOMER)
            .isEnabled(true)
            .isEmailVerified(false)
            .build();

        User savedUser = userRepository.save(user);
        log.info(
            "User registered successfully: {} (ID: {})",
            savedUser.getEmail(),
            savedUser.getId()
        );

        return generateAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository
            .findByEmail(request.email().toLowerCase().trim())
            .orElseThrow(() ->
                new BadCredentialsException("Invalid email or password")
            );

        if (!user.getIsEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (
            !passwordEncoder.matches(request.password(), user.getPasswordHash())
        ) {
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in: {} (ID: {})", user.getEmail(), user.getId());
        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            jwtDenylistService.addToDenylist(token);
            log.debug("User logged out, token denylisted");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException(
                "Invalid or expired refresh token"
            );
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid token type");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return generateAuthResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id)
            );
        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository
            .findByEmail(email.toLowerCase().trim())
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "User not found with email: " + email
                )
            );
        return mapToUserResponse(user);
    }

    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );
        Page<User> userPage = userRepository.findAll(pageable);
        return mapToPagedResponse(userPage);
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository
            .findByRole(role, Pageable.unpaged())
            .getContent()
            .stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id)
            );

        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }

        User savedUser = userRepository.save(user);
        log.info(
            "User updated: {} (ID: {})",
            savedUser.getEmail(),
            savedUser.getId()
        );
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public void changePassword(
        Long id,
        String currentPassword,
        String newPassword
    ) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id)
            );

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info(
            "Password changed for user: {} (ID: {})",
            user.getEmail(),
            user.getId()
        );
    }

    @Transactional
    public UserResponse updateUserRole(Long id, Role newRole) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id)
            );

        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        log.info(
            "User role updated: {} -> {} (ID: {})",
            savedUser.getEmail(),
            newRole,
            savedUser.getId()
        );
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public void toggleUserEnabled(Long id, boolean enabled) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id)
            );

        user.setIsEnabled(enabled);
        userRepository.save(user);
        log.info(
            "User {} status: enabled={} (ID: {})",
            user.getEmail(),
            enabled,
            user.getId()
        );
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            user.getId()
        );

        return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            accessToken,
            refreshToken,
            jwtTokenProvider.getAccessTokenExpirationMs() / 1000
        );
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getFullName(),
            user.getRole(),
            user.getIsEnabled(),
            user.getIsEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private PagedResponse<UserResponse> mapToPagedResponse(
        Page<User> userPage
    ) {
        List<UserResponse> content = userPage
            .getContent()
            .stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());

        return new PagedResponse<>(
            content,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.isFirst(),
            userPage.isLast()
        );
    }
}
