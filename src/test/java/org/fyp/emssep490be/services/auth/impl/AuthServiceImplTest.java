package org.fyp.emssep490be.services.auth.impl;

import org.fyp.emssep490be.configs.CustomUserDetails;
import org.fyp.emssep490be.configs.CustomUserDetailsService;
import org.fyp.emssep490be.configs.JwtTokenProvider;
import org.fyp.emssep490be.dtos.auth.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.repositories.UserBranchRepository;
import org.fyp.emssep490be.services.auth.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserBranchRepository userBranchRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserAccount testUser;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserAccount();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPhone("0123456789");
        testUser.setFullName("Test User");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setStatus("active");

        // Setup test user details
        testUserDetails = new CustomUserDetails(
                1L,
                "test@example.com",
                "$2a$10$hashedPassword",
                "Test User",
                List.of("STUDENT"),
                List.of(1L),
                true
        );
    }

    @Test
    void login_WithValidEmailAndPassword_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", null, "password123");

        Branch branch = new Branch();
        branch.setId(1L);
        branch.setCode("HN01");
        branch.setName("Hanoi Branch 1");

        UserBranch userBranch = new UserBranch();
        userBranch.setBranch(branch);

        Authentication testAuthentication = mock(Authentication.class);
        when(testAuthentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(testAuthentication);
        when(jwtTokenProvider.generateAccessToken(testUserDetails))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(testUserDetails))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .thenReturn(900L);
        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenReturn(testUser);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(List.of(userBranch));

        // Act
        LoginResponseDTO response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(900);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateAccessToken(testUserDetails);
        verify(jwtTokenProvider).generateRefreshToken(testUserDetails);
        verify(userAccountRepository).save(testUser);
    }

    @Test
    void login_WithValidPhoneAndPassword_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(null, "0123456789", "password123");

        CustomUserDetails phoneUserDetails = new CustomUserDetails(
                1L,
                "0123456789",
                "$2a$10$hashedPassword",
                "Test User",
                List.of("STUDENT"),
                List.of(1L),
                true
        );

        Authentication phoneAuthentication = mock(Authentication.class);
        when(phoneAuthentication.getPrincipal()).thenReturn(phoneUserDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(phoneAuthentication);
        when(jwtTokenProvider.generateAccessToken(phoneUserDetails))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(phoneUserDetails))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .thenReturn(900L);
        when(userAccountRepository.findByEmail("0123456789"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByPhone("0123456789"))
                .thenReturn(Optional.of(testUser));
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenReturn(testUser);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(List.of());

        // Act
        LoginResponseDTO response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_WithNoEmailOrPhone_ShouldThrowException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(null, null, "password123");

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email or phone is required");

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", null, "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewAccessToken() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.isTokenType("valid-refresh-token", "refresh")).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted("valid-refresh-token")).thenReturn(false);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token"))
                .thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(testUserDetails);
        when(jwtTokenProvider.generateAccessToken(testUserDetails))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .thenReturn(900L);

        // Act
        RefreshTokenResponseDTO response = authService.refreshToken(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getExpiresIn()).isEqualTo(900);

        verify(jwtTokenProvider).validateToken("valid-refresh-token");
        verify(jwtTokenProvider).isTokenType("valid-refresh-token", "refresh");
        verify(jwtTokenProvider).generateAccessToken(testUserDetails);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(jwtTokenProvider).validateToken("invalid-token");
        verify(jwtTokenProvider, never()).generateAccessToken(any(CustomUserDetails.class));
    }

    @Test
    void refreshToken_WithAccessToken_ShouldThrowException() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("access-token");

        when(jwtTokenProvider.validateToken("access-token")).thenReturn(true);
        when(jwtTokenProvider.isTokenType("access-token", "refresh")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a refresh token");

        verify(jwtTokenProvider).isTokenType("access-token", "refresh");
    }

    @Test
    void refreshToken_WithBlacklistedToken_ShouldThrowException() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("blacklisted-token");

        when(jwtTokenProvider.validateToken("blacklisted-token")).thenReturn(true);
        when(jwtTokenProvider.isTokenType("blacklisted-token", "refresh")).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted("blacklisted-token")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("revoked");

        verify(tokenBlacklistService).isTokenBlacklisted("blacklisted-token");
    }

    @Test
    void logout_WithValidToken_ShouldBlacklistToken() {
        // Arrange
        String token = "Bearer valid-token";

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);

        // Act
        authService.logout(token);

        // Assert
        verify(jwtTokenProvider).validateToken("valid-token");
        verify(tokenBlacklistService).blacklistToken("valid-token");
    }

    @Test
    void logout_WithTokenWithoutBearerPrefix_ShouldBlacklistToken() {
        // Arrange
        String token = "valid-token";

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);

        // Act
        authService.logout(token);

        // Assert
        verify(jwtTokenProvider).validateToken("valid-token");
        verify(tokenBlacklistService).blacklistToken("valid-token");
    }

    @Test
    void logout_WithInvalidToken_ShouldNotBlacklist() {
        // Arrange
        String token = "invalid-token";

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act
        authService.logout(token);

        // Assert
        verify(jwtTokenProvider).validateToken("invalid-token");
        verify(tokenBlacklistService, never()).blacklistToken(anyString());
    }
}
