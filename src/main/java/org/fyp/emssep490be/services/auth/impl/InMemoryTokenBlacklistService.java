package org.fyp.emssep490be.services.auth.impl;

import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.services.auth.TokenBlacklistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TokenBlacklistService
 * For production, consider using Redis for distributed systems
 */
@Service
@Slf4j
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    // Thread-safe set to store blacklisted tokens
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        log.info("Token added to blacklist. Total blacklisted: {}", blacklistedTokens.size());
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        // For in-memory implementation, we clear all tokens periodically
        // In production with Redis, this would remove only expired tokens
        int beforeSize = blacklistedTokens.size();
        blacklistedTokens.clear();
        log.info("Cleaned up blacklist. Removed {} tokens", beforeSize);
    }
}
