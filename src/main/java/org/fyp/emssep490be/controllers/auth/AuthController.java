package org.fyp.emssep490be.controllers.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("Login request received: {}", request);
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Login successful", response)
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
        RefreshTokenResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Token refreshed successfully", response)
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
    public ResponseEntity<ResponseObject<Void>> logout(
            @RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Logout successful", null)
        );
    }
}
