package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByClazzId(Long classId);
    Optional<Enrollment> findByStudentIdAndClazzId(Long studentId, Long classId);
    long countByClazzId(Long classId);
    boolean existsByStudentIdAndClazzId(Long studentId, Long classId);
}
