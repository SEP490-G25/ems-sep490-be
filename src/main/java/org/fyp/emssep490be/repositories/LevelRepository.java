package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findBySubjectIdOrderBySortOrderAsc(Long subjectId);

    Optional<Level> findByIdAndSubjectId(Long id, Long subjectId);

    boolean existsByCodeAndSubjectId(String code, Long subjectId);

    boolean existsBySortOrderAndSubjectId(Integer sortOrder, Long subjectId);

    /**
     * Count levels by subject ID
     */
    long countBySubjectId(Long subjectId);

    /**
     * Find level by ID with subject eagerly loaded
     */
    @Query("SELECT l FROM Level l LEFT JOIN FETCH l.subject WHERE l.id = :id")
    Optional<Level> findByIdWithSubject(@Param("id") Long id);
}
