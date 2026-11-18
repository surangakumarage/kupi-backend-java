package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.service.CustomTokenService;
import com.kupi.cc.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Token Endpoint Controller for Spring Boot 3.2.6
 *
 * Provides the standard OAuth2 /oauth/token endpoint for password grant flow.
 * This is a bridge between the Spring Security OAuth2 library and the
 * application's custom token services.
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
public class OAuth2TokenEndpointController {

    private final UserService userService;
    private final CustomTokenService customTokenService;
    private final DefaultTokenServices tokenServices;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth-service.client-secret:${CLIENT_SECRET}}")
    private String validClientSecret;

    public OAuth2TokenEndpointController(UserService userService,
                                        CustomTokenService customTokenService,
                                        DefaultTokenServices tokenServices,
                                        PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.customTokenService = customTokenService;
        this.tokenServices = tokenServices;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * OAuth2 Token Endpoint - Password & Refresh Token Grant Flow
     * Supports: grant_type=password with username and password
     *           grant_type=refresh_token with refresh token
     * Validates Basic Authentication header (client_id:client_secret)
     *
     * @param grantType    OAuth2 grant type (password or refresh_token)
     * @param username     Username/email (for password grant)
     * @param password     User password (for password grant)
     * @param refreshToken Refresh token (for refresh_token grant)
     * @param clientId     OAuth2 client ID
     * @param clientSecret OAuth2 client secret
     * @param request      HTTP request (for Authorization header)
     * @return OAuth2 token response
     */
    @PostMapping(path = "/token", produces = "application/json")
    public ResponseEntity<?> getToken(
            @RequestParam(value = "grant_type") String grantType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            HttpServletRequest request) {

        try {
            // Route to appropriate grant type handler
            if ("password".equals(grantType)) {
                return handlePasswordGrant(username, password, request);
            } else if ("refresh_token".equals(grantType)) {
                return handleRefreshTokenGrant(refreshToken, request);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "unsupported_grant_type",
                                "error_description", "Supported grant types: password, refresh_token"
                        ));
            }
        } catch (Exception e) {
            log.error("Token generation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "server_error",
                            "error_description", "Token generation failed"
                    ));
        }
    }

    /**
     * Handle password grant type
     */
    private ResponseEntity<?> handlePasswordGrant(String username, String password, HttpServletRequest request) {
        try {

            // Validate Basic Auth header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Missing or invalid Basic authentication header"
                        ));
            }

            // Decode Basic Auth header
            String[] basicAuth = decodeBasicAuth(authHeader);
            if (basicAuth == null || basicAuth.length != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Invalid Basic authentication format"
                        ));
            }

            String decodedClientId = basicAuth[0];
            String decodedClientSecret = basicAuth[1];

            // Validate client credentials
            if (!isValidClient(decodedClientId, decodedClientSecret)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Client authentication failed"
                        ));
            }

            // Validate required parameters
            if (username == null || password == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "invalid_request",
                                "error_description", "Missing username or password"
                        ));
            }

            // Authenticate user (both registered and guest users)
            // Java 11 compatibility: allows guest users to login with password grant
            var user = userService.getUser(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_grant",
                                "error_description", "Invalid username or password"
                        ));
            }

            // Validate user password
            if (!isValidUserPassword(user, password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_grant",
                                "error_description", "Invalid username or password"
                        ));
            }

            // Generate token (using existing token service)
            var tokenResponse = customTokenService.generateToken(user);
            String accessToken = tokenResponse.getAccessToken();
            String refreshTokenValue = tokenResponse.getRefreshToken();

            // Build response with user data
            return buildTokenResponse(user, accessToken, refreshTokenValue, true);

        } catch (Exception e) {
            log.error("Password grant failed", e);
            throw e;
        }
    }

    /**
     * Handle refresh token grant type
     */
    private ResponseEntity<?> handleRefreshTokenGrant(String refreshToken, HttpServletRequest request) {
        try {
            // Validate refresh token parameter
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "invalid_request",
                                "error_description", "Missing refresh_token parameter"
                        ));
            }

            // Validate Basic Auth header for client credentials
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Missing or invalid Basic authentication header"
                        ));
            }

            // Decode and validate client credentials
            String[] basicAuth = decodeBasicAuth(authHeader);
            if (basicAuth == null || basicAuth.length != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Invalid Basic authentication format"
                        ));
            }

            if (!isValidClient(basicAuth[0], basicAuth[1])) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "invalid_client",
                                "error_description", "Client authentication failed"
                        ));
            }

            // Refresh the token using token service
            // Create TokenRequest with client ID for refresh token validation
            Map<String, String> requestParameters = new HashMap<>();
            requestParameters.put("grant_type", "refresh_token");
            requestParameters.put("refresh_token", refreshToken);
            org.springframework.security.oauth2.provider.TokenRequest tokenRequest =
                new org.springframework.security.oauth2.provider.TokenRequest(requestParameters, basicAuth[0], new java.util.HashSet<>(), "refresh_token");
            var newTokenResponse = tokenServices.refreshAccessToken(refreshToken, tokenRequest);
            if (newTokenResponse == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "invalid_grant",
                                "error_description", "Invalid or expired refresh token"
                        ));
            }

            String newAccessToken = newTokenResponse.getValue();
            String newRefreshToken = newTokenResponse.getRefreshToken().getValue();

            // Return token response (without user data for refresh flow)
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", newAccessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("scope", "READ WRITE UPDATE");
            if (newRefreshToken != null && !newRefreshToken.isEmpty()) {
                response.put("refresh_token", newRefreshToken);
            }
            response.put("message", "Token refreshed successfully");
            response.put("status", "SUCCESS");

            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Refresh token grant failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "invalid_grant",
                            "error_description", "Token refresh failed"
                    ));
        }
    }

    /**
     * Build complete token response with user data
     */
    private ResponseEntity<?> buildTokenResponse(User user, String accessToken, String refreshToken, boolean includeUserData) {
        Map<String, Object> response = new HashMap<>();

        // OAuth2 standard fields
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);
        response.put("scope", "READ WRITE UPDATE");

        // Add refresh token if available
        if (refreshToken != null && !refreshToken.isEmpty()) {
            response.put("refresh_token", refreshToken);
        }

        // Add user data wrapper (only for password grant, not refresh token)
        if (includeUserData) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getId() != null ? user.getId() : "");
            userData.put("fullName", user.getFullName() != null ? user.getFullName() : "");
            userData.put("imageUrl", user.getImageUrl() != null ? user.getImageUrl() : "");
            userData.put("language", user.getLanguage() != null ? user.getLanguage() : "ENGLISH");
            userData.put("approvalStatus", user.getApprovalStatus() != null ? user.getApprovalStatus() : "");

            // Add role information if available
            if (user.getRole() != null) {
                Map<String, Object> roleData = new HashMap<>();
                roleData.put("id", user.getRole().getId());
                roleData.put("name", user.getRole().getName());
                roleData.put("permissions", user.getRole().getPermissions() != null ? user.getRole().getPermissions() : new java.util.ArrayList<>());
                userData.put("role", roleData);
            }

            userData.put("active", user.isEnabled());
            response.put("data", userData);
        }

        // Add status fields
        response.put("message", "Successfully logged-in the user");
        response.put("status", "SUCCESS");

        return ResponseEntity.ok(response);
    }

    /**
     * Decode Basic Authentication header
     * Format: "Basic base64(clientId:clientSecret)"
     */
    private String[] decodeBasicAuth(String authHeader) {
        try {
            String encoded = authHeader.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(encoded));
            return decoded.split(":", 2);
        } catch (Exception e) {
            log.error("Failed to decode Basic auth header", e);
            return null;
        }
    }

    /**
     * Validate client credentials
     * In production, check against registered OAuth2 clients
     */
    private boolean isValidClient(String clientId, String clientSecret) {
        // Hardcoded for demo - in production, query database
        String validClientId = "tokomobile";

        boolean isValid = validClientId.equals(clientId) && validClientSecret.equals(clientSecret);
        if (!isValid) {
            log.warn("Invalid client credentials attempted for clientId: {}", clientId);
        }
        return isValid;
    }

    /**
     * Validate user password using PasswordEncoder
     * Compares the provided password against the user's stored (hashed) password
     */
    private boolean isValidUserPassword(Object user, String password) {
        try {
            if (user == null || password == null || password.trim().isEmpty()) {
                return false;
            }

            // Get password from user entity
            String storedPassword = null;
            if (user instanceof User) {
                storedPassword = ((User) user).getPassword();
            } else {
                log.error("User object is not of expected type");
                return false;
            }

            // Validate password using PasswordEncoder
            if (storedPassword == null) {
                log.warn("No stored password found for user");
                return false;
            }

            boolean isPasswordValid = passwordEncoder.matches(password, storedPassword);
            if (!isPasswordValid) {
                log.warn("Invalid password provided for user");
            }
            return isPasswordValid;

        } catch (Exception e) {
            log.error("Failed to validate user password", e);
            return false;
        }
    }

    /**
     * Token Check Endpoint - Validates existing tokens
     * Used by resource servers to validate access tokens
     *
     * @param token Access token to validate
     * @return Token info if valid
     */
    @PostMapping(path = "/check_token", produces = "application/json")
    public ResponseEntity<?> checkToken(
            @RequestParam(value = "token") String token) {

        try {
            // Validate token using existing service
            boolean isExpired = userService.readAccessToken(token);

            if (isExpired) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "invalid_token",
                                "error_description", "Token has expired"
                        ));
            }

            // Return token info
            Map<String, Object> response = new HashMap<>();
            response.put("active", true);
            response.put("exp", System.currentTimeMillis() / 1000 + 3600);
            response.put("scope", "read write");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "invalid_token",
                            "error_description", "Token validation failed"
                    ));
        }
    }
}
