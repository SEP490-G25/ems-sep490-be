package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.UserBranch;
import org.fyp.emssep490be.entities.ids.UserBranchId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserBranch entity
 * Handles database operations for user-branch associations (multi-branch access control)
 */
@Repository
public interface UserBranchRepository extends JpaRepository<UserBranch, UserBranchId> {

    /**
     * Find all branches for a specific user
     *
     * @param userId User ID
     * @return List of UserBranch associations
     */
    @Query("SELECT ub FROM UserBranch ub JOIN FETCH ub.branch WHERE ub.id.userId = :userId")
    List<UserBranch> findByUserId(@Param("userId") Long userId);

    /**
     * Find all users assigned to a specific branch
     *
     * @param branchId Branch ID
     * @return List of UserBranch associations
     */
    @Query("SELECT ub FROM UserBranch ub JOIN FETCH ub.user WHERE ub.id.branchId = :branchId")
    List<UserBranch> findByBranchId(@Param("branchId") Long branchId);

    /**
     * Delete all branch assignments for a specific user
     *
     * @param userId User ID
     */
    void deleteByUserId(Long userId);

    /**
     * Delete all user assignments for a specific branch
     *
     * @param branchId Branch ID
     */
    void deleteByBranchId(Long branchId);
}
