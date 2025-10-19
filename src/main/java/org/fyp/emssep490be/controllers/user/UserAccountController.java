package org.fyp.emssep490be.controllers.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.user.ChangePasswordRequestDTO;
import org.fyp.emssep490be.dtos.user.CreateUserRequestDTO;
import org.fyp.emssep490be.dtos.user.UpdateUserRequestDTO;
import org.fyp.emssep490be.dtos.user.UserDTO;
import org.fyp.emssep490be.services.user.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for User Account management operations
 * Base path: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    /**
     * Get All Users
     * GET /users
     *
     * @param status Filter by status (active|inactive)
     * @param roleId Filter by role ID
     * @param branchId Filter by branch ID
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 20)
     * @return Paginated list of users
     */
    @GetMapping
    public ResponseEntity<ResponseObject<PagedResponseDTO<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        PagedResponseDTO<UserDTO> result = userAccountService.getAllUsers(status, roleId, branchId, page, limit);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Users retrieved successfully", result)
        );
    }

    /**
     * Get User Detail
     * GET /users/{id}
     *
     * @param id User ID
     * @return Detailed user information including roles and branches
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO result = userAccountService.getUserById(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "User retrieved successfully", result)
        );
    }

    /**
     * Create User
     * POST /users
     * Roles: ADMIN
     *
     * @param request User creation data
     * @return Created user information
     */
    @PostMapping
    public ResponseEntity<ResponseObject<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequestDTO request) {
        UserDTO result = userAccountService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "User created successfully", result)
        );
    }

    /**
     * Update User
     * PUT /users/{id}
     * Roles: ADMIN
     *
     * @param id User ID
     * @param request User update data
     * @return Updated user information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        UserDTO result = userAccountService.updateUser(id, request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "User updated successfully", result)
        );
    }

    /**
     * Delete User (soft delete)
     * DELETE /users/{id}
     * Roles: ADMIN
     *
     * @param id User ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAccountService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Change Password
     * PUT /users/{id}/change-password
     * Users can change their own password
     *
     * @param id User ID
     * @param request Password change data
     * @return Success message
     */
    @PutMapping("/{id}/change-password")
    public ResponseEntity<ResponseObject<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        userAccountService.changePassword(id, request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Password changed successfully", null)
        );
    }

    /**
     * Activate User
     * PUT /users/{id}/activate
     * Roles: ADMIN
     *
     * @param id User ID
     * @return Success message
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseObject<Void>> activateUser(@PathVariable Long id) {
        userAccountService.activateUser(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "User activated successfully", null)
        );
    }

    /**
     * Deactivate User
     * PUT /users/{id}/deactivate
     * Roles: ADMIN
     *
     * @param id User ID
     * @return Success message
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseObject<Void>> deactivateUser(@PathVariable Long id) {
        userAccountService.deactivateUser(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "User deactivated successfully", null)
        );
    }
}
