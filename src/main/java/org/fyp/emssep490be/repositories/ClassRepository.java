package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for ClassEntity
 * Handles database operations for class management
 */
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    /**
     * Find class by code and branch
     *
     * @param code     Class code
     * @param branchId Branch ID
     * @return Optional containing class if found
     */
    Optional<ClassEntity> findByCodeAndBranchId(String code, Long branchId);

    /**
     * Check if class code exists for a branch
     *
     * @param code     Class code
     * @param branchId Branch ID
     * @return true if class code exists
     */
    boolean existsByCodeAndBranchId(String code, Long branchId);

    /**
     * Find all classes with complex filtering
     *
     * @param branchId      Filter by branch ID
     * @param courseId      Filter by course ID
     * @param status        Filter by status
     * @param modality      Filter by modality
     * @param startDateFrom Filter by start date from
     * @param startDateTo   Filter by start date to
     * @param pageable      Pagination parameters
     * @return Page of classes
     */
    @Query("SELECT c FROM ClassEntity c WHERE " +
            "(:branchId IS NULL OR c.branch.id = :branchId) AND " +
            "(:courseId IS NULL OR c.course.id = :courseId) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:modality IS NULL OR c.modality = :modality) AND " +
            "(:startDateFrom IS NULL OR c.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR c.startDate <= :startDateTo)")
    Page<ClassEntity> findByFilters(@Param("branchId") Long branchId,
                                     @Param("courseId") Long courseId,
                                     @Param("status") String status,
                                     @Param("modality") String modality,
                                     @Param("startDateFrom") LocalDate startDateFrom,
                                     @Param("startDateTo") LocalDate startDateTo,
                                     Pageable pageable);

    /**
     * Find class by ID with related entities
     *
     * @param id Class ID
     * @return Optional containing class with branch and course
     */
    @Query("SELECT c FROM ClassEntity c " +
            "LEFT JOIN FETCH c.branch " +
            "LEFT JOIN FETCH c.course " +
            "WHERE c.id = :id")
    Optional<ClassEntity> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find classes by branch and status
     *
     * @param branchId Branch ID
     * @param status   Class status
     * @return List of classes
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.branch.id = :branchId AND c.status = :status")
    Page<ClassEntity> findByBranchIdAndStatus(@Param("branchId") Long branchId,
                                               @Param("status") String status,
                                               Pageable pageable);

    /**
     * Count classes by course
     *
     * @param courseId Course ID
     * @return Number of classes
     */
    long countByCourseId(Long courseId);

    /**
     * Count classes by branch
     *
     * @param branchId Branch ID
     * @return Number of classes
     */
    long countByBranchId(Long branchId);
}
