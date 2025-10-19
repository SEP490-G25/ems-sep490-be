package org.fyp.emssep490be.services.user;

import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.user.ChangePasswordRequestDTO;
import org.fyp.emssep490be.dtos.user.CreateUserRequestDTO;
import org.fyp.emssep490be.dtos.user.UpdateUserRequestDTO;
import org.fyp.emssep490be.dtos.user.UserDTO;

/**
 * Service interface for UserAccount management operations
 */
public interface UserAccountService {

    /**
     * Get all users with pagination and filtering
     *
     * @param status Filter by status
     * @param roleId Filter by role ID
     * @param branchId Filter by branch ID
     * @param page Page number
     * @param limit Items per page
     * @return Paginated list of users
     */
    PagedResponseDTO<UserDTO> getAllUsers(String status, Long roleId, Long branchId, Integer page, Integer limit);

    /**
     * Get user by ID with detailed information
     *
     * @param id User ID
     * @return User details including roles and branches
     */
    UserDTO getUserById(Long id);

    /**
     * Create a new user account
     * Only ADMIN can create users
     *
     * @param request User creation data
     * @return Created user information
     */
    UserDTO createUser(CreateUserRequestDTO request);

    /**
     * Update an existing user account
     * ADMIN can update any user
     *
     * @param id User ID
     * @param request User update data
     * @return Updated user information
     */
    UserDTO updateUser(Long id, UpdateUserRequestDTO request);

    /**
     * Delete a user account (soft delete - set status to inactive)
     * ADMIN only
     *
     * @param id User ID
     */
    void deleteUser(Long id);

    /**
     * Change user password
     * Users can change their own password
     *
     * @param userId User ID
     * @param request Password change data
     */
    void changePassword(Long userId, ChangePasswordRequestDTO request);

    /**
     * Activate a user account
     * ADMIN only
     *
     * @param id User ID
     */
    void activateUser(Long id);

    /**
     * Deactivate a user account
     * ADMIN only
     *
     * @param id User ID
     */
    void deactivateUser(Long id);
}
