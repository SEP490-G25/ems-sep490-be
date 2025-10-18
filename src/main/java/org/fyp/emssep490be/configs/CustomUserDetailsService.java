package org.fyp.emssep490be.configs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.entities.UserBranch;
import org.fyp.emssep490be.entities.UserRole;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.repositories.UserBranchRepository;
import org.fyp.emssep490be.repositories.UserRoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Custom UserDetailsService implementation
 * Loads user by email or phone with roles and branch access
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserBranchRepository userBranchRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Try to find by email first, then by phone
        UserAccount user = userAccountRepository.findByEmail(username)
                .or(() -> userAccountRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Load user roles
        List<String> roles = userRoleRepository.findByUserId(user.getId())
                .stream()
                .map(UserRole::getRole)
                .map(role -> role.getCode())
                .toList();

        // Load user branches
        List<Long> branchIds = userBranchRepository.findByUserId(user.getId())
                .stream()
                .map(UserBranch::getBranch)
                .map(branch -> branch.getId())
                .toList();

        boolean enabled = "active".equalsIgnoreCase(user.getStatus());

        log.debug("User loaded: id={}, roles={}, branches={}", user.getId(), roles, branchIds);

        return new CustomUserDetails(
                user.getId(),
                username,
                user.getPasswordHash(),
                user.getFullName(),
                roles,
                branchIds,
                enabled
        );
    }

    /**
     * Load user by email
     *
     * @param email User email
     * @return UserDetails
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return loadUserByUsername(email);
    }

    /**
     * Load user by phone
     *
     * @param phone User phone
     * @return UserDetails
     */
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        return loadUserByUsername(phone);
    }
}
