package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for UserAccount entity
 * Handles database operations for user authentication
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Find user by email address
     *
     * @param email User email
     * @return Optional containing user if found
     */
    Optional<UserAccount> findByEmail(String email);

    /**
     * Find user by phone number
     *
     * @param phone User phone number
     * @return Optional containing user if found
     */
    Optional<UserAccount> findByPhone(String phone);

    /**
     * Check if email already exists
     *
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number already exists
     *
     * @param phone Phone number to check
     * @return true if phone exists
     */
    boolean existsByPhone(String phone);
}
