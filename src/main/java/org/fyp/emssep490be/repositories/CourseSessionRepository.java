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
    List<CourseSession> findByPhaseIdOrderBySequenceNoAsc(Long phaseId);
    Optional<CourseSession> findByIdAndPhaseId(Long id, Long phaseId);
    boolean existsBySequenceNoAndPhaseId(Integer sequenceNo, Long phaseId);

    @Query("SELECT cs FROM CourseSession cs " +
            "LEFT JOIN FETCH cs.cloMappings " +
            "WHERE cs.phase.id = :phaseId " +
            "ORDER BY cs.sequenceNo ASC")
    List<CourseSession> findByPhaseIdWithClos(@Param("phaseId") Long phaseId);
}
