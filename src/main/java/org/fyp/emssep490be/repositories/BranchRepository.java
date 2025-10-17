package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Branch entity
 * Handles database operations for branch management
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    /**
     * Find all branches by center entity
     *
     * @param center Center entity
     * @return List of branches for the center
     */
    List<Branch> findByCenter(Center center);

    /**
     * Find branch by code and center
     *
     * @param code     Branch code
     * @param centerId Center ID
     * @return Optional containing branch if found
     */
    Optional<Branch> findByCodeAndCenterId(String code, Long centerId);

    /**
     * Find all branches by center with pagination
     *
     * @param centerId Center ID
     * @param pageable Pagination parameters
     * @return Page of branches
     */
    Page<Branch> findByCenterId(Long centerId, Pageable pageable);

    /**
     * Find all branches by status with pagination
     *
     * @param status   Branch status
     * @param pageable Pagination parameters
     * @return Page of branches
     */
    Page<Branch> findByStatus(String status, Pageable pageable);

    /**
     * Find all branches by center and status with pagination
     *
     * @param centerId Center ID
     * @param status   Branch status
     * @param pageable Pagination parameters
     * @return Page of branches
     */
    Page<Branch> findByCenterIdAndStatus(Long centerId, String status, Pageable pageable);

    /**
     * Check if branch code exists for a center
     *
     * @param code     Branch code
     * @param centerId Center ID
     * @return true if branch code exists
     */
    boolean existsByCodeAndCenterId(String code, Long centerId);
}
