package org.fyp.emssep490be.services.auth;

import org.fyp.emssep490be.dtos.auth.LoginRequestDTO;
import org.fyp.emssep490be.dtos.auth.LoginResponseDTO;
import org.fyp.emssep490be.dtos.auth.RefreshTokenRequestDTO;
import org.fyp.emssep490be.dtos.auth.RefreshTokenResponseDTO;

/**
 * Service interface for Authentication & Authorization operations
 */
public interface AuthService {

    /**
     * Authenticates user with email/phone and password
     *
     * @param request Login credentials
     * @return JWT tokens and user information
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * Refreshes access token using refresh token
     *
     * @param request Refresh token
     * @return New access token and expiration time
     */
    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request);

    /**
     * Invalidates user session and token
     *
     * @param token Bearer token to invalidate
     */
    void logout(String token);
}
