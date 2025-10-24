package org.fyp.emssep490be.repositories;

import java.util.List;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.fyp.emssep490be.entities.ids.TeacherSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TeacherSkillRepository extends JpaRepository<TeacherSkill, TeacherSkillId> {
    
    @Query("SELECT ts FROM TeacherSkill ts WHERE ts.id.teacherId = :teacherId")
    List<TeacherSkill> findByTeacherId(@Param("teacherId") Long teacherId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TeacherSkill ts WHERE ts.id.teacherId = :teacherId")
    void deleteByTeacherId(@Param("teacherId") Long teacherId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO teacher_skill (teacher_id, skill, level) VALUES (:teacherId, CAST(:skill AS skill_enum), :level)", nativeQuery = true)
    void insertTeacherSkill(@Param("teacherId") Long teacherId,
                            @Param("skill") String skillLower,
                            @Param("level") Short level);
    
    @Query(value = "SELECT teacher_id, skill, level FROM teacher_skill WHERE teacher_id = :teacherId", nativeQuery = true)
    List<Object[]> findSkillsByTeacherIdNative(@Param("teacherId") Long teacherId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM teacher_skill WHERE teacher_id = :teacherId AND skill::text = :skill", nativeQuery = true)
    void deleteByTeacherIdAndSkill(@Param("teacherId") Long teacherId, @Param("skill") String skill);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE teacher_skill SET level = :level WHERE teacher_id = :teacherId AND skill::text = :skill", nativeQuery = true)
    void updateTeacherSkillLevel(@Param("teacherId") Long teacherId, @Param("skill") String skill, @Param("level") Short level);
}
