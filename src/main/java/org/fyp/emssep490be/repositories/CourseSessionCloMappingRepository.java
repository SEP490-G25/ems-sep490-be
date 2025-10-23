package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.CourseSessionCloMapping;
import org.fyp.emssep490be.entities.ids.CourseSessionCloMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSessionCloMappingRepository extends JpaRepository<CourseSessionCloMapping, CourseSessionCloMappingId> {

    /**
     * Find all mappings for a specific CourseSession
     *
     * @param courseSessionId CourseSession ID
     * @return List of CourseSession-CLO mappings
     */
    @Query("SELECT m FROM CourseSessionCloMapping m WHERE m.id.courseSessionId = :courseSessionId")
    List<CourseSessionCloMapping> findByCourseSessionId(@Param("courseSessionId") Long courseSessionId);

    /**
     * Find all mappings for a specific CLO
     *
     * @param cloId CLO ID
     * @return List of CourseSession-CLO mappings
     */
    @Query("SELECT m FROM CourseSessionCloMapping m WHERE m.id.cloId = :cloId")
    List<CourseSessionCloMapping> findByCloId(@Param("cloId") Long cloId);

    /**
     * Check if a mapping exists between CourseSession and CLO
     *
     * @param courseSessionId CourseSession ID
     * @param cloId CLO ID
     * @return true if mapping exists
     */
    boolean existsByCourseSessionIdAndCloId(Long courseSessionId, Long cloId);

    /**
     * Check if any mappings exist for a CourseSession
     *
     * @param courseSessionId CourseSession ID
     * @return true if mappings exist
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM CourseSessionCloMapping m WHERE m.id.courseSessionId = :courseSessionId")
    boolean existsByCourseSessionId(@Param("courseSessionId") Long courseSessionId);

    /**
     * Check if any mappings exist for a CLO
     *
     * @param cloId CLO ID
     * @return true if mappings exist
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM CourseSessionCloMapping m WHERE m.id.cloId = :cloId")
    boolean existsByCloId(@Param("cloId") Long cloId);

    /**
     * Count mappings for a specific CourseSession
     *
     * @param courseSessionId CourseSession ID
     * @return Number of mappings
     */
    @Query("SELECT COUNT(m) FROM CourseSessionCloMapping m WHERE m.id.courseSessionId = :courseSessionId")
    long countByCourseSessionId(@Param("courseSessionId") Long courseSessionId);

    /**
     * Count mappings for a specific CLO
     *
     * @param cloId CLO ID
     * @return Number of mappings
     */
    @Query("SELECT COUNT(m) FROM CourseSessionCloMapping m WHERE m.id.cloId = :cloId")
    long countByCloId(@Param("cloId") Long cloId);

    /**
     * Delete all mappings for a specific CourseSession
     *
     * @param courseSessionId CourseSession ID
     */
    @Query("DELETE FROM CourseSessionCloMapping m WHERE m.id.courseSessionId = :courseSessionId")
    void deleteByCourseSessionId(@Param("courseSessionId") Long courseSessionId);

    /**
     * Delete all mappings for a specific CLO
     *
     * @param cloId CLO ID
     */
    @Query("DELETE FROM CourseSessionCloMapping m WHERE m.id.cloId = :cloId")
    void deleteByCloId(@Param("cloId") Long cloId);
}
