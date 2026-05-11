package com.musicstore.user.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
@Tag(name = "Authentication", description = "OAuth2 social login endpoints")
public class OAuth2Controller {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Operation(summary = "List available OAuth2 providers",
               description = "Returns the list of configured OAuth2 providers and their login URLs")
    @GetMapping("/providers")
    public ResponseEntity<Map<String, String>> getProviders() {
        return ResponseEntity.ok(Map.of(
                "google", "/oauth2/authorization/google",
                "github", "/oauth2/authorization/github"
        ));
    }

    @Operation(summary = "Get frontend OAuth2 callback URL",
               description = "Returns the frontend URL that OAuth2 will redirect to after login")
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getOAuth2Config() {
        return ResponseEntity.ok(Map.of(
                "frontendUrl", frontendUrl,
                "callbackPath", frontendUrl + "/oauth2/callback",
                "providers", List.of("google", "github")
        ));
    }
}
