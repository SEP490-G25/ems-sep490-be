package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.TimeSlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TimeSlotTemplate entity
 * Handles database operations for time slot template management
 */
@Repository
public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, Long> {

    /**
     * Find all time slots for a branch
     *
     * @param branchId Branch ID
     * @return List of time slots
     */
    List<TimeSlotTemplate> findByBranchIdOrderByStartTimeAsc(Long branchId);

    /**
     * Find time slot by ID and branch
     *
     * @param id       Time slot ID
     * @param branchId Branch ID
     * @return Optional containing time slot if found
     */
    Optional<TimeSlotTemplate> findByIdAndBranchId(Long id, Long branchId);

    /**
     * Check if time slot exists for a branch
     *
     * @param branchId Branch ID
     * @param name     Time slot name
     * @return true if time slot exists
     */
    boolean existsByBranchIdAndName(Long branchId, String name);

    /**
     * Find overlapping time slots for a branch
     *
     * @param branchId  Branch ID
     * @param startTime Start time
     * @param endTime   End time
     * @return List of overlapping time slots
     */
    @Query("SELECT t FROM TimeSlotTemplate t WHERE t.branch.id = :branchId " +
            "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<TimeSlotTemplate> findOverlappingTimeSlots(
            @Param("branchId") Long branchId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
