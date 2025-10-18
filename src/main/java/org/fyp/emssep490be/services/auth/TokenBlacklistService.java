package org.fyp.emssep490be.services.auth;

/**
 * Service interface for managing blacklisted JWT tokens (logout functionality)
 */
public interface TokenBlacklistService {

    /**
     * Add token to blacklist (invalidate token)
     *
     * @param token JWT token to blacklist
     */
    void blacklistToken(String token);

    /**
     * Check if token is blacklisted
     *
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Remove expired tokens from blacklist (cleanup)
     */
    void cleanupExpiredTokens();
}
