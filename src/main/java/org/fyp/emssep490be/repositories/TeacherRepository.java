package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmployeeCode(String employeeCode);

    Optional<Teacher> findByUserAccountId(Long userAccountId);

    @Query("SELECT t FROM Teacher t JOIN FETCH t.userAccount WHERE t.id = :id")
    Optional<Teacher> findByIdWithUserAccount(@Param("id") Long id);
}
