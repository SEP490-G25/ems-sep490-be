package org.fyp.emssep490be.repositories;

import java.util.List;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherSkillRepository extends JpaRepository<TeacherSkill, Long> {
    List<TeacherSkill> findByTeacherId(Long teacherId);
}

