package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.StudentSession;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSessionRepository extends JpaRepository<StudentSession, StudentSessionId>, JpaSpecificationExecutor<StudentSession> {
    List<StudentSession> findByIdStudentId(Long studentId);
    List<StudentSession> findByIdSessionId(Long sessionId);
    Optional<StudentSession> findByIdStudentIdAndIdSessionId(Long studentId, Long sessionId);

    long countByIdStudentIdAndAttendanceStatus(Long studentId, String attendanceStatus);
}
