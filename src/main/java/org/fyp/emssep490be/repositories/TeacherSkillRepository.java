package org.fyp.emssep490be.repositories;

import java.util.List;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.fyp.emssep490be.entities.ids.TeacherSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherSkillRepository extends JpaRepository<TeacherSkill, TeacherSkillId> {
    List<TeacherSkill> findByTeacherId(Long teacherId);
}
