package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.TeachingSlot;
import org.fyp.emssep490be.entities.enums.Skill;
import org.fyp.emssep490be.entities.ids.TeachingSlotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingSlotRepository extends JpaRepository<TeachingSlot, TeachingSlotId> {

    List<TeachingSlot> findByIdSessionId(Long sessionId);

    Optional<TeachingSlot> findByIdSessionIdAndIdTeacherIdAndIdSkill(Long sessionId, Long teacherId, Skill skill);

    void deleteByIdSessionIdAndIdTeacherId(Long sessionId, Long teacherId);

    @Query("SELECT ts FROM TeachingSlot ts WHERE ts.teacher.id = :teacherId AND " +
            "ts.session.date = :date AND " +
            "((ts.session.startTime < :endTime AND ts.session.endTime > :startTime))")
    List<TeachingSlot> findConflictingSlots(@Param("teacherId") Long teacherId,
                                             @Param("date") LocalDate date,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime);

    long countByIdTeacherIdAndSessionDate(Long teacherId, LocalDate date);
}
