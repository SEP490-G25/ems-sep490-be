package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    Page<Subject> findByStatus(SubjectStatus status, Pageable pageable);

    /**
     * Find subject by ID with levels eagerly loaded
     */
    @Query("SELECT s FROM Subject s LEFT JOIN FETCH s.createdBy WHERE s.id = :id")
    Optional<Subject> findByIdWithDetails(@Param("id") Long id);
}
