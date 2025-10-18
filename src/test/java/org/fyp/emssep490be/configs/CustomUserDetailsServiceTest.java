package org.fyp.emssep490be.configs;

import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.repositories.UserBranchRepository;
import org.fyp.emssep490be.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserBranchRepository userBranchRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UserAccount testUser;
    private List<UserRole> testUserRoles;
    private List<UserBranch> testUserBranches;

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

        // Setup test roles
        Role studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setCode("STUDENT");
        studentRole.setName("Student");

        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(studentRole);

        testUserRoles = List.of(userRole);

        // Setup test branches
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setCode("HN01");
        branch.setName("Hanoi Branch 1");

        UserBranch userBranch = new UserBranch();
        userBranch.setUser(testUser);
        userBranch.setBranch(branch);

        testUserBranches = List.of(userBranch);
    }

    @Test
    void loadUserByUsername_WithEmail_ShouldReturnUserDetails() {
        // Arrange
        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(customUserDetails.getUserId()).isEqualTo(1L);
        assertThat(customUserDetails.getFullName()).isEqualTo("Test User");
        assertThat(customUserDetails.getRoles()).containsExactly("STUDENT");
        assertThat(customUserDetails.getBranchIds()).containsExactly(1L);
        assertThat(customUserDetails.isEnabled()).isTrue();

        verify(userAccountRepository).findByEmail("test@example.com");
        verify(userRoleRepository).findByUserId(1L);
        verify(userBranchRepository).findByUserId(1L);
    }

    @Test
    void loadUserByUsername_WithPhone_ShouldReturnUserDetails() {
        // Arrange
        when(userAccountRepository.findByEmail("0123456789"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByPhone("0123456789"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("0123456789");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUsername()).isEqualTo("0123456789");
        assertThat(customUserDetails.getUserId()).isEqualTo(1L);

        verify(userAccountRepository).findByEmail("0123456789");
        verify(userAccountRepository).findByPhone("0123456789");
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userAccountRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByPhone(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userAccountRepository).findByEmail("nonexistent@example.com");
        verify(userAccountRepository).findByPhone("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ShouldReturnDisabledUserDetails() {
        // Arrange
        testUser.setStatus("inactive");
        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_WithMultipleRoles_ShouldReturnAllRoles() {
        // Arrange
        Role teacherRole = new Role();
        teacherRole.setId(2L);
        teacherRole.setCode("TEACHER");
        teacherRole.setName("Teacher");

        UserRole userRole2 = new UserRole();
        userRole2.setUser(testUser);
        userRole2.setRole(teacherRole);

        List<UserRole> multipleRoles = List.of(testUserRoles.get(0), userRole2);

        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(multipleRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getRoles()).containsExactlyInAnyOrder("STUDENT", "TEACHER");
    }

    @Test
    void loadUserByUsername_WithMultipleBranches_ShouldReturnAllBranches() {
        // Arrange
        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setCode("HN02");
        branch2.setName("Hanoi Branch 2");

        UserBranch userBranch2 = new UserBranch();
        userBranch2.setUser(testUser);
        userBranch2.setBranch(branch2);

        List<UserBranch> multipleBranches = List.of(testUserBranches.get(0), userBranch2);

        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(multipleBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getBranchIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void loadUserByEmail_ShouldCallLoadUserByUsername() {
        // Arrange
        when(userAccountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByEmail("test@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserByPhone_ShouldCallLoadUserByUsername() {
        // Arrange
        when(userAccountRepository.findByEmail("0123456789"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.findByPhone("0123456789"))
                .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(1L))
                .thenReturn(testUserRoles);
        when(userBranchRepository.findByUserId(1L))
                .thenReturn(testUserBranches);

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByPhone("0123456789");

        // Assert
        assertThat(userDetails).isNotNull();
    }
}
