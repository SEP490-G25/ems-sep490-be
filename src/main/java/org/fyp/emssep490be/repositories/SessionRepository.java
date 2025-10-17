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

    @Query("SELECT s FROM SessionEntity s WHERE s.clazz.id = :classId AND " +
            "(:dateFrom IS NULL OR s.date >= :dateFrom) AND " +
            "(:dateTo IS NULL OR s.date <= :dateTo) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:type IS NULL OR s.type = :type)")
    Page<SessionEntity> findByClassIdWithFilters(@Param("classId") Long classId,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo,
                                                  @Param("status") String status,
                                                  @Param("type") String type,
                                                  Pageable pageable);

    List<SessionEntity> findByClazzIdAndDateAfter(Long classId, LocalDate date);

    @Query(value = "SELECT * FROM session s WHERE s.class_id = :classId AND " +
            "s.date >= :effectiveFrom AND " +
            "EXTRACT(DOW FROM s.date) = :dayOfWeek", nativeQuery = true)
    List<SessionEntity> findByClassIdAndDateFromAndDayOfWeek(@Param("classId") Long classId,
                                                               @Param("effectiveFrom") LocalDate effectiveFrom,
                                                               @Param("dayOfWeek") Integer dayOfWeek);

    long countByClazzId(Long classId);

    long countByClazzIdAndStatus(Long classId, String status);
}
