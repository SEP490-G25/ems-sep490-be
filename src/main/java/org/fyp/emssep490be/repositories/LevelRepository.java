package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findBySubjectIdOrderBySortOrderAsc(Long subjectId);
    Optional<Level> findByIdAndSubjectId(Long id, Long subjectId);
    boolean existsByCodeAndSubjectId(String code, Long subjectId);
}
