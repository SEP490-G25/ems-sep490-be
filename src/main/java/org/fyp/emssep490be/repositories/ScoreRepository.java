package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByAssessmentId(Long assessmentId);
    Optional<Score> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
}
