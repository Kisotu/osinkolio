package com.musicstore.user.security;

import com.musicstore.user.domain.entity.Role;
import com.musicstore.user.domain.entity.User;
import com.musicstore.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtDenylistService jwtDenylistService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        log.info("OAuth2 login success: provider={}, name={}", provider, oauth2User.getName());

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = extractEmail(attributes, provider);
        String firstName = extractFirstName(attributes, provider);
        String lastName = extractLastName(attributes, provider);

        if (email == null || email.isBlank()) {
            log.error("OAuth2 provider {} did not return an email", provider);
            response.sendRedirect(frontendUrl + "/oauth2/error?message=" +
                    URLEncoder.encode("Email not provided by " + provider, StandardCharsets.UTF_8));
            return;
        }

        // Find existing user or create a new one
        User user = findOrCreateUser(email, firstName, lastName);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationMs() / 1000;

        // Redirect to frontend with tokens
        String redirectUrl = String.format(
                "%s/oauth2/callback?accessToken=%s&refreshToken=%s&expiresIn=%d",
                frontendUrl, accessToken, refreshToken, expiresIn);

        log.info("OAuth2 login complete for user: {} (ID: {}), redirecting to frontend", email, user.getId());
        response.sendRedirect(redirectUrl);
    }

    private User findOrCreateUser(String email, String firstName, String lastName) {
        Optional<User> existingUser = userRepository.findByEmail(email.toLowerCase().trim());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.getIsEnabled()) {
                throw new IllegalStateException("Account is disabled");
            }
            log.debug("Existing OAuth2 user found: {}", email);
            return user;
        }

        // Create new user from OAuth2 data
        User newUser = User.builder()
                .email(email.toLowerCase().trim())
                .passwordHash("") // OAuth2 users don't have a password
                .firstName(firstName != null ? firstName.trim() : email.split("@")[0])
                .lastName(lastName != null ? lastName.trim() : "")
                .role(Role.CUSTOMER)
                .isEnabled(true)
                .isEmailVerified(true) // OAuth2 providers verify email
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user created via OAuth2: {} (ID: {})", email, savedUser.getId());
        return savedUser;
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        return switch (provider) {
            case "google" -> (String) attributes.get("email");
            case "github" -> {
                // GitHub may not expose email in user info; try primary verified from /user/emails
                String ghEmail = (String) attributes.get("email");
                yield ghEmail != null ? ghEmail : attributes.get("login") + "@github.oauth";
            }
            default -> (String) attributes.get("email");
        };
    }

    private String extractFirstName(Map<String, Object> attributes, String provider) {
        return switch (provider) {
            case "google" -> (String) attributes.get("given_name");
            case "github" -> (String) attributes.get("login");
            default -> (String) attributes.get("name");
        };
    }

    private String extractLastName(Map<String, Object> attributes, String provider) {
        return switch (provider) {
            case "google" -> (String) attributes.get("family_name");
            default -> "";
        };
    }
}
