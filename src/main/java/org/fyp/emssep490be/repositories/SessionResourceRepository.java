package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.SessionResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionResourceRepository extends JpaRepository<SessionResource, Long> {

    List<SessionResource> findBySessionId(Long sessionId);

    Optional<SessionResource> findBySessionIdAndResourceId(Long sessionId, Long resourceId);

    void deleteBySessionIdAndResourceId(Long sessionId, Long resourceId);

    @Query("SELECT sr FROM SessionResource sr WHERE sr.resource.id = :resourceId AND " +
            "sr.session.sessionDate = :date AND " +
            "((sr.session.startTime < :endTime AND sr.session.endTime > :startTime))")
    List<SessionResource> findConflictingResources(@Param("resourceId") Long resourceId,
                                                    @Param("date") LocalDate date,
                                                    @Param("startTime") LocalTime startTime,
                                                    @Param("endTime") LocalTime endTime);
}
