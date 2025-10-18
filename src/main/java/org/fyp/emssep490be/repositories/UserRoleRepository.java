package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.UserRole;
import org.fyp.emssep490be.entities.ids.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserRole entity
 * Handles database operations for user-role associations
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * Find all roles for a specific user
     *
     * @param userId User ID
     * @return List of UserRole associations
     */
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.id.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);

    /**
     * Find all users with a specific role
     *
     * @param roleId Role ID
     * @return List of UserRole associations
     */
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user WHERE ur.id.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);

    /**
     * Delete all roles for a specific user
     *
     * @param userId User ID
     */
    void deleteByUserId(Long userId);
}
