package org.fyp.emssep490be.services.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.configs.CustomUserDetails;
import org.fyp.emssep490be.configs.CustomUserDetailsService;
import org.fyp.emssep490be.configs.JwtTokenProvider;
import org.fyp.emssep490be.dtos.auth.*;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.entities.UserBranch;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.repositories.UserBranchRepository;
import org.fyp.emssep490be.services.auth.AuthService;
import org.fyp.emssep490be.services.auth.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementation of AuthService for Authentication & Authorization operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserBranchRepository userBranchRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Authenticates user with email/phone and password
     *
     * @param request Login credentials
     * @return JWT tokens and user information
     */
    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.getEmail() != null ? request.getEmail() : request.getPhone());

        // Determine username (email or phone)
        String username = StringUtils.hasText(request.getEmail())
                ? request.getEmail()
                : request.getPhone();

        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Email or phone is required");
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );

        // Load full user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Update last login time
        UserAccount user = userAccountRepository.findByEmail(username)
                .or(() -> userAccountRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setLastLoginAt(OffsetDateTime.now());
        userAccountRepository.save(user);

        // Build user info DTO
        UserInfoDTO userInfo = buildUserInfoDTO(userDetails, user);

        log.info("Login successful for user: {}", username);

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                (int) jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userInfo
        );
    }

    /**
     * Refreshes access token using refresh token
     *
     * @param request Refresh token
     * @return New access token and expiration time
     */
    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        log.info("Refreshing access token");

        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Check if it's actually a refresh token
        if (!jwtTokenProvider.isTokenType(refreshToken, "refresh")) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }

        // Check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        // Get username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        log.info("Access token refreshed for user: {}", username);

        return new RefreshTokenResponseDTO(
                newAccessToken,
                (int) jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    /**
     * Invalidates user session and token
     *
     * @param token Bearer token to invalidate
     */
    @Override
    public void logout(String token) {
        log.info("Logout requested");

        // Remove "Bearer " prefix if present
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        // Validate token before blacklisting
        if (jwtTokenProvider.validateToken(jwtToken)) {
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(jwtToken);
            log.info("Token blacklisted successfully");
        } else {
            log.warn("Attempted to logout with invalid token");
        }
    }

    /**
     * Build UserInfoDTO from CustomUserDetails and UserAccount
     *
     * @param userDetails CustomUserDetails
     * @param user        UserAccount
     * @return UserInfoDTO
     */
    private UserInfoDTO buildUserInfoDTO(CustomUserDetails userDetails, UserAccount user) {
        // Load branch information
        List<BranchInfoDTO> branches = userBranchRepository.findByUserId(user.getId())
                .stream()
                .map(UserBranch::getBranch)
                .map(branch -> new BranchInfoDTO(
                        branch.getId(),
                        branch.getCode(),
                        branch.getName()
                ))
                .toList();

        return new UserInfoDTO(
                userDetails.getUserId(),
                user.getEmail(),
                user.getFullName(),
                userDetails.getRoles(),
                branches
        );
    }
}
