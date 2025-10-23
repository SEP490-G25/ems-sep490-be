package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.StudentSession;
import org.fyp.emssep490be.entities.enums.AttendanceStatus;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSessionRepository extends JpaRepository<StudentSession, StudentSessionId>, JpaSpecificationExecutor<StudentSession> {
    List<StudentSession> findByIdStudentId(Long studentId);
    List<StudentSession> findByIdSessionId(Long sessionId);
    Optional<StudentSession> findByIdStudentIdAndIdSessionId(Long studentId, Long sessionId);

    long countByIdStudentIdAndAttendanceStatus(Long studentId, String attendanceStatus);

    /**
     * Find sessions where student was absent or has planned status (eligible for makeup).
     * Only returns sessions from planned status sessions (not yet occurred).
     *
     * @param studentId the student ID
     * @return list of student sessions eligible for makeup
     */
    @Query("SELECT ss FROM StudentSession ss " +
           "JOIN FETCH ss.session s " +
           "JOIN FETCH s.courseSession cs " +
           "JOIN FETCH s.clazz c " +
           "WHERE ss.student.id = :studentId " +
           "AND ss.attendanceStatus IN ('ABSENT', 'PLANNED') " +
           "AND s.status = 'PLANNED' " +
           "ORDER BY s.date DESC")
    List<StudentSession> findMissedSessionsEligibleForMakeup(@Param("studentId") Long studentId);

    /**
     * Check if student has a schedule conflict at the given date and time.
     *
     * @param studentId the student ID
     * @param date the session date
     * @param startTime the start time
     * @param endTime the end time
     * @return count of conflicting sessions (> 0 means conflict exists)
     */
    @Query("SELECT COUNT(ss) FROM StudentSession ss " +
           "JOIN ss.session s " +
           "WHERE ss.student.id = :studentId " +
           "AND s.date = :date " +
           "AND ss.attendanceStatus != 'EXCUSED' " +
           "AND (" +
           "  (s.startTime < :endTime AND s.endTime > :startTime)" +
           ")")
    long countScheduleConflicts(@Param("studentId") Long studentId,
                                 @Param("date") LocalDate date,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime);

    /**
     * Count the number of makeup sessions a student has used (approved makeup requests).
     *
     * @param studentId the student ID
     * @param classId the class ID (optional, for class-specific quota)
     * @return count of makeup sessions
     */
    @Query("SELECT COUNT(ss) FROM StudentSession ss " +
           "JOIN ss.session s " +
           "WHERE ss.student.id = :studentId " +
           "AND ss.isMakeup = true " +
           "AND (:classId IS NULL OR s.clazz.id = :classId)")
    long countMakeupSessions(@Param("studentId") Long studentId,
                             @Param("classId") Long classId);
}
