package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.CoursePhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePhaseRepository extends JpaRepository<CoursePhase, Long> {
    List<CoursePhase> findByCourseIdOrderBySortOrderAsc(Long courseId);
    Optional<CoursePhase> findByIdAndCourseId(Long id, Long courseId);
    boolean existsByPhaseNumberAndCourseId(Integer phaseNumber, Long courseId);
    long countByCourseId(Long courseId);
}
