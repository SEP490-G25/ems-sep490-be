package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
    List<CourseSession> findByPhaseIdOrderBySequenceNumberAsc(Long phaseId);
    Optional<CourseSession> findByIdAndPhaseId(Long id, Long phaseId);
    boolean existsBySequenceNumberAndPhaseId(Integer sequenceNumber, Long phaseId);
}
