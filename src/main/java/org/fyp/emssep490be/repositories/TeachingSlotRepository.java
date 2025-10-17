package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.TeachingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingSlotRepository extends JpaRepository<TeachingSlot, Long> {

    List<TeachingSlot> findBySessionId(Long sessionId);

    Optional<TeachingSlot> findBySessionIdAndTeacherIdAndSkill(Long sessionId, Long teacherId, String skill);

    void deleteBySessionIdAndTeacherId(Long sessionId, Long teacherId);

    @Query("SELECT ts FROM TeachingSlot ts WHERE ts.teacher.id = :teacherId AND " +
            "ts.session.sessionDate = :date AND " +
            "((ts.session.startTime < :endTime AND ts.session.endTime > :startTime))")
    List<TeachingSlot> findConflictingSlots(@Param("teacherId") Long teacherId,
                                             @Param("date") LocalDate date,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime);

    long countByTeacherIdAndSessionSessionDate(Long teacherId, LocalDate date);
}
