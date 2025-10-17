package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Clo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloRepository extends JpaRepository<Clo, Long> {
    List<Clo> findByCourseId(Long courseId);
    Optional<Clo> findByIdAndCourseId(Long id, Long courseId);
    boolean existsByCodeAndCourseId(String code, Long courseId);

    @Query("SELECT c FROM Clo c " +
            "LEFT JOIN FETCH c.ploMappings pm " +
            "LEFT JOIN FETCH pm.plo " +
            "WHERE c.course.id = :courseId")
    List<Clo> findByCourseIdWithPlos(@Param("courseId") Long courseId);
}
