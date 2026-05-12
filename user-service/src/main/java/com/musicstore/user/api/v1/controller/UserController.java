package com.musicstore.user.api.v1.controller;

import com.musicstore.user.api.v1.dto.*;
import com.musicstore.user.domain.entity.Role;
import com.musicstore.user.security.UserPrincipal;
import com.musicstore.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Endpoints for user profile management"
)
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user's profile by their ID"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
        }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(
        summary = "Get user by email",
        description = "Retrieves a user's profile by their email address"
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
        @Parameter(description = "Email address") @PathVariable String email
    ) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
        summary = "Get all users",
        description = "Retrieves a paginated list of all users (admin only)"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "List of users"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
        }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(
        summary = "Get users by role",
        description = "Retrieves users filtered by role (admin only)"
    )
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
        @Parameter(description = "User role") @PathVariable Role role
    ) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @Operation(
        summary = "Update current user",
        description = "Updates the authenticated user's profile"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
        }
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(
            userService.updateUser(principal.userId(), request)
        );
    }

    @Operation(
        summary = "Update any user (admin)",
        description = "Updates any user's profile (admin only)"
    )
    @PutMapping("/admin/{userId}")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long userId,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(
        summary = "Change password",
        description = "Changes the authenticated user's password"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Password changed"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Current password is incorrect"
            ),
        }
    )
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(
            principal.userId(),
            request.currentPassword(),
            request.newPassword()
        );
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Update user role (admin)",
        description = "Updates a user's role (admin only)"
    )
    @PutMapping("/admin/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
        @PathVariable Long userId,
        @RequestParam Role role
    ) {
        return ResponseEntity.ok(userService.updateUserRole(userId, role));
    }

    @Operation(
        summary = "Toggle user enabled (admin)",
        description = "Enables or disables a user account (admin only)"
    )
    @PutMapping("/admin/{userId}/enabled")
    public ResponseEntity<Void> toggleUserEnabled(
        @PathVariable Long userId,
        @RequestParam boolean enabled
    ) {
        userService.toggleUserEnabled(userId, enabled);
        return ResponseEntity.ok().build();
    }
}
