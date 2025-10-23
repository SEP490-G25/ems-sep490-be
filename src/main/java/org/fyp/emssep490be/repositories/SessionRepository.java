package org.fyp.emssep490be.repositories;

import jakarta.persistence.LockModeType;
import org.fyp.emssep490be.entities.SessionEntity;
import org.fyp.emssep490be.entities.enums.Modality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    @Query("SELECT s FROM SessionEntity s WHERE s.clazz.id = :classId AND " +
            "(:dateFrom IS NULL OR s.date >= :dateFrom) AND " +
            "(:dateTo IS NULL OR s.date <= :dateTo) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:type IS NULL OR s.type = :type)")
    Page<SessionEntity> findByClassIdWithFilters(@Param("classId") Long classId,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo,
                                                  @Param("status") String status,
                                                  @Param("type") String type,
                                                  Pageable pageable);

    List<SessionEntity> findByClazzIdAndDateAfter(Long classId, LocalDate date);

    @Query(value = "SELECT * FROM session s WHERE s.class_id = :classId AND " +
            "s.date >= :effectiveFrom AND " +
            "EXTRACT(DOW FROM s.date) = :dayOfWeek", nativeQuery = true)
    List<SessionEntity> findByClassIdAndDateFromAndDayOfWeek(@Param("classId") Long classId,
                                                               @Param("effectiveFrom") LocalDate effectiveFrom,
                                                               @Param("dayOfWeek") Integer dayOfWeek);

    long countByClazzId(Long classId);

    long countByClazzIdAndStatus(Long classId, String status);

    /**
     * Find available makeup sessions for a given course session ID.
     * This query finds sessions with the same course content (course_session_id)
     * that have available capacity and are scheduled in the future.
     *
     * @param courseSessionId the course session ID to match
     * @param studentId the student ID (to exclude sessions they're already enrolled in)
     * @param dateFrom optional earliest date filter
     * @param dateTo optional latest date filter
     * @param branchId optional branch filter
     * @param modality optional modality filter
     * @return list of available sessions with capacity information
     */
    @Query("SELECT s, c, b, cs, " +
           "(c.maxCapacity - COALESCE(COUNT(ss.id.studentId), 0)) as availableSlots, " +
           "COALESCE(COUNT(ss.id.studentId), 0) as enrolledCount " +
           "FROM SessionEntity s " +
           "JOIN s.clazz c " +
           "JOIN c.branch b " +
           "JOIN s.courseSession cs " +
           "LEFT JOIN StudentSession ss ON s.id = ss.id.sessionId " +
           "  AND ss.attendanceStatus != 'EXCUSED' " +
           "WHERE s.courseSession.id = :courseSessionId " +
           "AND s.status = 'PLANNED' " +
           "AND s.date >= CURRENT_DATE " +
           "AND (:dateFrom IS NULL OR s.date >= :dateFrom) " +
           "AND (:dateTo IS NULL OR s.date <= :dateTo) " +
           "AND (:branchId IS NULL OR b.id = :branchId) " +
           "AND (:modality IS NULL OR c.modality = :modality) " +
           "AND s.id NOT IN (" +
           "  SELECT ss2.id.sessionId FROM StudentSession ss2 " +
           "  WHERE ss2.id.studentId = :studentId" +
           ") " +
           "GROUP BY s.id, c.id, b.id, cs.id " +
           "HAVING COUNT(ss.id.studentId) < c.maxCapacity " +
           "ORDER BY (c.maxCapacity - COUNT(ss.id.studentId)) DESC, s.date ASC")
    List<Object[]> findAvailableMakeupSessions(@Param("courseSessionId") Long courseSessionId,
                                                @Param("studentId") Long studentId,
                                                @Param("dateFrom") LocalDate dateFrom,
                                                @Param("dateTo") LocalDate dateTo,
                                                @Param("branchId") Long branchId,
                                                @Param("modality") Modality modality);

    /**
     * Count current enrollment in a session (excluding excused students).
     *
     * @param sessionId the session ID
     * @return count of enrolled students
     */
    @Query("SELECT COUNT(ss) FROM StudentSession ss " +
           "WHERE ss.id.sessionId = :sessionId " +
           "AND ss.attendanceStatus != 'EXCUSED'")
    long countEnrolledStudents(@Param("sessionId") Long sessionId);

    /**
     * Find session by ID with pessimistic write lock.
     * This prevents concurrent modifications to the same session during critical operations
     * such as makeup request approvals and capacity checks.
     * 
     * USE CASES:
     * - Makeup request approval: Lock session to prevent concurrent capacity changes
     * - Session updates: Lock to prevent concurrent modifications
     * 
     * @param sessionId the session ID
     * @return optional session with exclusive lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SessionEntity s WHERE s.id = :sessionId")
    Optional<SessionEntity> findByIdWithLock(@Param("sessionId") Long sessionId);
}
