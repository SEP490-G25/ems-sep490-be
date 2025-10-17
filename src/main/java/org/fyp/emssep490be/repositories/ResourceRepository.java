package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Resource entity
 * Handles database operations for resource (rooms & virtual) management
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Find all resources for a branch
     *
     * @param branchId Branch ID
     * @return List of resources
     */
    List<Resource> findByBranchId(Long branchId);

    /**
     * Find all resources by branch and type
     *
     * @param branchId     Branch ID
     * @param resourceType Resource type (ROOM|VIRTUAL)
     * @return List of resources
     */
    List<Resource> findByBranchIdAndResourceType(Long branchId, String resourceType);

    /**
     * Find resource by ID and branch
     *
     * @param id       Resource ID
     * @param branchId Branch ID
     * @return Optional containing resource if found
     */
    Optional<Resource> findByIdAndBranchId(Long id, Long branchId);

    /**
     * Check if resource name exists for a branch
     *
     * @param branchId Branch ID
     * @param name     Resource name
     * @return true if resource exists
     */
    boolean existsByBranchIdAndName(Long branchId, String name);

    /**
     * Find available resources for a branch on a specific date and time
     *
     * @param branchId     Branch ID
     * @param resourceType Resource type filter (optional)
     * @param date         Date to check availability
     * @param startTime    Start time
     * @param endTime      End time
     * @return List of available resources
     */
    @Query("SELECT r FROM Resource r WHERE r.branch.id = :branchId " +
            "AND (:resourceType IS NULL OR r.resourceType = :resourceType) " +
            "AND r.id NOT IN (" +
            "  SELECT sr.resource.id FROM SessionResource sr " +
            "  WHERE sr.session.date = :date " +
            "  AND ((sr.session.startTime < :endTime AND sr.session.endTime > :startTime))" +
            ")")
    List<Resource> findAvailableResources(
            @Param("branchId") Long branchId,
            @Param("resourceType") String resourceType,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
