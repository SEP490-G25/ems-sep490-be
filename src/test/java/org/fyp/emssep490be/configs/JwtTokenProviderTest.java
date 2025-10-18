package org.fyp.emssep490be.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtTokenProvider
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "TestSecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm12345";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                TEST_SECRET,
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION
        );
    }

    @Test
    void generateAccessToken_WithUserDetails_ShouldReturnValidToken() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();

        // Act
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isTokenType(token, "access")).isTrue();
    }

    @Test
    void generateAccessToken_WithAuthentication_ShouldReturnValidToken() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isTokenType(token, "access")).isTrue();
    }

    @Test
    void generateRefreshToken_WithUserDetails_ShouldReturnValidToken() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();

        // Act
        String token = jwtTokenProvider.generateRefreshToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isTokenType(token, "refresh")).isTrue();
    }

    @Test
    void generateRefreshToken_WithAuthentication_ShouldReturnValidToken() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtTokenProvider.generateRefreshToken(authentication);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isTokenType(token, "refresh")).isTrue();
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenType_WithAccessToken_ShouldReturnTrueForAccessType() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Act
        boolean isAccessToken = jwtTokenProvider.isTokenType(token, "access");
        boolean isRefreshToken = jwtTokenProvider.isTokenType(token, "refresh");

        // Assert
        assertThat(isAccessToken).isTrue();
        assertThat(isRefreshToken).isFalse();
    }

    @Test
    void isTokenType_WithRefreshToken_ShouldReturnTrueForRefreshType() {
        // Arrange
        CustomUserDetails userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateRefreshToken(userDetails);

        // Act
        boolean isAccessToken = jwtTokenProvider.isTokenType(token, "access");
        boolean isRefreshToken = jwtTokenProvider.isTokenType(token, "refresh");

        // Assert
        assertThat(isAccessToken).isFalse();
        assertThat(isRefreshToken).isTrue();
    }

    @Test
    void isTokenType_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token";

        // Act
        boolean result = jwtTokenProvider.isTokenType(invalidToken, "access");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void getAccessTokenExpirationInSeconds_ShouldReturnCorrectValue() {
        // Act
        long expirationInSeconds = jwtTokenProvider.getAccessTokenExpirationInSeconds();

        // Assert
        assertThat(expirationInSeconds).isEqualTo(ACCESS_TOKEN_EXPIRATION / 1000);
    }

    @Test
    void getRefreshTokenExpirationInSeconds_ShouldReturnCorrectValue() {
        // Act
        long expirationInSeconds = jwtTokenProvider.getRefreshTokenExpirationInSeconds();

        // Assert
        assertThat(expirationInSeconds).isEqualTo(REFRESH_TOKEN_EXPIRATION / 1000);
    }

    @Test
    void generateAccessToken_WithRoles_ShouldIncludeRolesInToken() {
        // Arrange
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "test@example.com",
                "password",
                "Test User",
                List.of("ADMIN", "TEACHER"),
                List.of(1L, 2L),
                true
        );

        // Act
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        // Roles should be embedded in the token
    }

    /**
     * Helper method to create test user details
     */
    private CustomUserDetails createTestUserDetails() {
        return new CustomUserDetails(
                1L,
                "test@example.com",
                "password",
                "Test User",
                List.of("STUDENT"),
                List.of(1L),
                true
        );
    }
}
