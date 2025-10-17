package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.SessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    @Query("SELECT s FROM SessionEntity s WHERE s.classEntity.id = :classId AND " +
            "(:dateFrom IS NULL OR s.sessionDate >= :dateFrom) AND " +
            "(:dateTo IS NULL OR s.sessionDate <= :dateTo) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:type IS NULL OR s.type = :type)")
    Page<SessionEntity> findByClassIdWithFilters(@Param("classId") Long classId,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo,
                                                  @Param("status") String status,
                                                  @Param("type") String type,
                                                  Pageable pageable);

    @Query("SELECT s FROM SessionEntity s " +
            "LEFT JOIN FETCH s.teachingSlots ts " +
            "LEFT JOIN FETCH ts.teacher " +
            "LEFT JOIN FETCH s.sessionResources sr " +
            "LEFT JOIN FETCH sr.resource " +
            "WHERE s.id = :id")
    Optional<SessionEntity> findByIdWithDetails(@Param("id") Long id);

    List<SessionEntity> findByClassEntityIdAndSessionDateAfter(Long classId, LocalDate date);

    @Query("SELECT s FROM SessionEntity s WHERE s.classEntity.id = :classId AND " +
            "s.sessionDate >= :effectiveFrom AND " +
            "FUNCTION('EXTRACT', DAY_OF_WEEK, s.sessionDate) = :dayOfWeek")
    List<SessionEntity> findByClassIdAndDateFromAndDayOfWeek(@Param("classId") Long classId,
                                                               @Param("effectiveFrom") LocalDate effectiveFrom,
                                                               @Param("dayOfWeek") Integer dayOfWeek);

    long countByClassEntityId(Long classId);

    long countByClassEntityIdAndStatus(Long classId, String status);
}
