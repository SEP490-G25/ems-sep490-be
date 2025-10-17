package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByCourseId(Long courseId);
    List<CourseMaterial> findByPhaseId(Long phaseId);
    List<CourseMaterial> findByCourseSessionId(Long courseSessionId);
    Optional<CourseMaterial> findByIdAndCourseId(Long id, Long courseId);
}
