package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Plo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PloRepository extends JpaRepository<Plo, Long> {
    List<Plo> findBySubjectId(Long subjectId);
    Optional<Plo> findByIdAndSubjectId(Long id, Long subjectId);
    boolean existsByCodeAndSubjectId(String code, Long subjectId);
}
