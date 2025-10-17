package org.fyp.emssep490be.controllers.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.auth.LoginRequestDTO;
import org.fyp.emssep490be.dtos.auth.LoginResponseDTO;
import org.fyp.emssep490be.dtos.auth.RefreshTokenRequestDTO;
import org.fyp.emssep490be.dtos.auth.RefreshTokenResponseDTO;
import org.fyp.emssep490be.services.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Authentication & Authorization operations
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 1.1 Login
     * POST /auth/login
     * Authenticates user with email/phone and password
     *
     * @param request Login credentials
     * @return JWT tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseObject<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        // TODO: Implement login logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Login successful", null)
        );
    }

    /**
     * 1.2 Refresh Token
     * POST /auth/refresh
     * Refreshes access token using refresh token
     *
     * @param request Refresh token
     * @return New access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject<RefreshTokenResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        // TODO: Implement refresh token logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Token refreshed successfully", null)
        );
    }

    /**
     * 1.3 Logout
     * POST /auth/logout
     * Invalidates current user session
     *
     * @param authorization Bearer token from header
     * @return No content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorization) {
        // TODO: Implement logout logic
        return ResponseEntity.noContent().build();
    }
}
