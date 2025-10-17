package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.StudentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSessionRepository extends JpaRepository<StudentSession, Long> {
    List<StudentSession> findByEnrollmentId(Long enrollmentId);
    List<StudentSession> findBySessionId(Long sessionId);
    Optional<StudentSession> findByEnrollmentIdAndSessionId(Long enrollmentId, Long sessionId);

    @Query("SELECT ss FROM StudentSession ss WHERE ss.enrollment.student.id = :studentId")
    List<StudentSession> findByStudentId(@Param("studentId") Long studentId);

    long countByEnrollmentIdAndAttendanceStatus(Long enrollmentId, String attendanceStatus);
}
