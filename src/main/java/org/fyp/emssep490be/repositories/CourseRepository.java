package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT c FROM Course c WHERE " +
            "(:subjectId IS NULL OR c.subject.id = :subjectId) AND " +
            "(:levelId IS NULL OR c.level.id = :levelId) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:approved IS NULL OR (:approved = true AND c.approvedByManager IS NOT NULL) OR (:approved = false AND c.approvedByManager IS NULL))")
    Page<Course> findByFilters(@Param("subjectId") Long subjectId,
                                @Param("levelId") Long levelId,
                                @Param("status") String status,
                                @Param("approved") Boolean approved,
                                Pageable pageable);

    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.coursePhases " +
            "LEFT JOIN FETCH c.clos " +
            "WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") Long id);
}
