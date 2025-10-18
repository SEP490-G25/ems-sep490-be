package org.fyp.emssep490be.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Branch Access Checker for multi-branch access control
 * Used with @PreAuthorize for method-level security
 */
@Component("branchAccessChecker")
@Slf4j
public class BranchAccessChecker {

    /**
     * Check if authenticated user can access the specified branch
     *
     * @param authentication Spring Security Authentication
     * @param branchId       Branch ID to check access for
     * @return true if user has access to the branch, false otherwise
     */
    public boolean canAccessBranch(Authentication authentication, Long branchId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication is null or not authenticated");
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            log.warn("Principal is not an instance of CustomUserDetails");
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;

        // Check if user has access to the branch
        boolean hasAccess = userDetails.getBranchIds().contains(branchId);

        log.debug("User {} {} access to branch {}",
                userDetails.getUsername(),
                hasAccess ? "has" : "does not have",
                branchId);

        return hasAccess;
    }

    /**
     * Check if authenticated user can access any of the specified branches
     *
     * @param authentication Spring Security Authentication
     * @param branchIds      List of branch IDs to check access for
     * @return true if user has access to any of the branches, false otherwise
     */
    public boolean canAccessAnyBranch(Authentication authentication, Long... branchIds) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;

        for (Long branchId : branchIds) {
            if (userDetails.getBranchIds().contains(branchId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if authenticated user can access all of the specified branches
     *
     * @param authentication Spring Security Authentication
     * @param branchIds      List of branch IDs to check access for
     * @return true if user has access to all branches, false otherwise
     */
    public boolean canAccessAllBranches(Authentication authentication, Long... branchIds) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;

        for (Long branchId : branchIds) {
            if (!userDetails.getBranchIds().contains(branchId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the authenticated user's branch IDs
     *
     * @param authentication Spring Security Authentication
     * @return List of branch IDs the user has access to
     */
    public java.util.List<Long> getUserBranchIds(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Collections.emptyList();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return java.util.Collections.emptyList();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        return userDetails.getBranchIds();
    }
}
