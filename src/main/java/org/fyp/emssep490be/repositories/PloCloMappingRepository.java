package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.PloCloMapping;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PloCloMappingRepository extends JpaRepository<PloCloMapping, PloCloMappingId> {

    /**
     * Find all mappings for a specific PLO
     *
     * @param ploId PLO ID
     * @return List of PLO-CLO mappings
     */
    @Query("SELECT m FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    List<PloCloMapping> findByPloId(@Param("ploId") Long ploId);

    /**
     * Find all mappings for a specific CLO
     *
     * @param cloId CLO ID
     * @return List of PLO-CLO mappings
     */
    @Query("SELECT m FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    List<PloCloMapping> findByCloId(@Param("cloId") Long cloId);

    /**
     * Check if a mapping exists between PLO and CLO
     *
     * @param ploId PLO ID
     * @param cloId CLO ID
     * @return true if mapping exists
     */
    boolean existsByPloIdAndCloId(Long ploId, Long cloId);

    /**
     * Check if any mappings exist for a PLO
     *
     * @param ploId PLO ID
     * @return true if mappings exist
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    boolean existsByPloId(@Param("ploId") Long ploId);

    /**
     * Count mappings for a specific PLO
     *
     * @param ploId PLO ID
     * @return Number of mappings
     */
    @Query("SELECT COUNT(m) FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    long countByPloId(@Param("ploId") Long ploId);

    /**
     * Count mappings for a specific CLO
     *
     * @param cloId CLO ID
     * @return Number of mappings
     */
    @Query("SELECT COUNT(m) FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    long countByCloId(@Param("cloId") Long cloId);

    /**
     * Delete all mappings for a specific PLO
     *
     * @param ploId PLO ID
     */
    @Query("DELETE FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    void deleteByPloId(@Param("ploId") Long ploId);

    /**
     * Delete all mappings for a specific CLO
     *
     * @param cloId CLO ID
     */
    @Query("DELETE FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    void deleteByCloId(@Param("cloId") Long cloId);
}
