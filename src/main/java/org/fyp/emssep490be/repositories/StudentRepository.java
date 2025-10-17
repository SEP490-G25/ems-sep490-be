package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByUserAccountId(Long userAccountId);
    boolean existsByStudentCode(String studentCode);
}
