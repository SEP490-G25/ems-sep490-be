package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Center entity.
 * Provides CRUD operations and custom queries for managing training centers.
 */
@Repository
public interface CenterRepository extends JpaRepository<Center, Long> {

    /**
     * Find a center by its unique code.
     *
     * @param code the center code
     * @return Optional containing the center if found
     */
    Optional<Center> findByCode(String code);

    /**
     * Check if a center exists by code.
     *
     * @param code the center code
     * @return true if center exists, false otherwise
     */
    boolean existsByCode(String code);
}
