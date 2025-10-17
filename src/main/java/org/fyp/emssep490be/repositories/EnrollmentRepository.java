package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByClassEntityId(Long classId);
    Optional<Enrollment> findByStudentIdAndClassEntityId(Long studentId, Long classId);
    long countByClassEntityId(Long classId);
    boolean existsByStudentIdAndClassEntityId(Long studentId, Long classId);
}
