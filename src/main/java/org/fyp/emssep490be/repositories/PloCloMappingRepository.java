package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.PloCloMapping;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

<<<<<<< HEAD
/**
 * Repository for PLO-CLO mapping entity
 */
=======
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
@Repository
public interface PloCloMappingRepository extends JpaRepository<PloCloMapping, PloCloMappingId> {

    /**
<<<<<<< HEAD
     * Find all mappings for a PLO
=======
     * Find all mappings for a specific PLO
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     *
     * @param ploId PLO ID
     * @return List of PLO-CLO mappings
     */
    @Query("SELECT m FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    List<PloCloMapping> findByPloId(@Param("ploId") Long ploId);

    /**
<<<<<<< HEAD
     * Find all mappings for a CLO
=======
     * Find all mappings for a specific CLO
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     *
     * @param cloId CLO ID
     * @return List of PLO-CLO mappings
     */
    @Query("SELECT m FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    List<PloCloMapping> findByCloId(@Param("cloId") Long cloId);

    /**
<<<<<<< HEAD
     * Check if mapping exists between PLO and CLO
=======
     * Check if a mapping exists between PLO and CLO
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     *
     * @param ploId PLO ID
     * @param cloId CLO ID
     * @return true if mapping exists
     */
<<<<<<< HEAD
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM PloCloMapping m WHERE m.id.ploId = :ploId AND m.id.cloId = :cloId")
    boolean existsByPloIdAndCloId(@Param("ploId") Long ploId, @Param("cloId") Long cloId);

    /**
     * Count mappings for a PLO
     *
     * @param ploId PLO ID
     * @return Count of CLOs mapped to this PLO
     */
    @Query("SELECT COUNT(m) FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    long countByPloId(@Param("ploId") Long ploId);

    /**
     * Count mappings for a CLO
     *
     * @param cloId CLO ID
     * @return Count of PLOs mapped to this CLO
     */
    @Query("SELECT COUNT(m) FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    long countByCloId(@Param("cloId") Long cloId);

    /**
     * Check if PLO has any CLO mappings
     *
     * @param ploId PLO ID
     * @return true if PLO has mappings
=======
    boolean existsByPloIdAndCloId(Long ploId, Long cloId);

    /**
     * Check if any mappings exist for a PLO
     *
     * @param ploId PLO ID
     * @return true if mappings exist
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    boolean existsByPloId(@Param("ploId") Long ploId);

    /**
<<<<<<< HEAD
     * Check if CLO has any PLO mappings
     *
     * @param cloId CLO ID
     * @return true if CLO has mappings
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    boolean existsByCloId(@Param("cloId") Long cloId);

    /**
     * Delete all mappings for a PLO
=======
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
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     *
     * @param ploId PLO ID
     */
    @Query("DELETE FROM PloCloMapping m WHERE m.id.ploId = :ploId")
    void deleteByPloId(@Param("ploId") Long ploId);

    /**
<<<<<<< HEAD
     * Delete all mappings for a CLO
=======
     * Delete all mappings for a specific CLO
>>>>>>> 760c99eca11e1a162483449d8339ef5765642161
     *
     * @param cloId CLO ID
     */
    @Query("DELETE FROM PloCloMapping m WHERE m.id.cloId = :cloId")
    void deleteByCloId(@Param("cloId") Long cloId);
}
