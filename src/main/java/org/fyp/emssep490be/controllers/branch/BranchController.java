package org.fyp.emssep490be.controllers.branch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.branch.BranchDTO;
import org.fyp.emssep490be.dtos.branch.BranchDetailDTO;
import org.fyp.emssep490be.dtos.branch.CreateBranchRequestDTO;
import org.fyp.emssep490be.dtos.branch.UpdateBranchRequestDTO;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.services.branch.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Branch management operations
 * Base path: /api/v1/branches
 */
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * Get All Branches
     * GET /branches
     *
     * @param centerId Filter by center ID
     * @param status   Filter by status (active|inactive|closed|planned)
     * @param page     Page number (default: 1)
     * @param limit    Items per page (default: 20)
     * @return Paginated list of branches
     */
    @GetMapping
    public ResponseEntity<ResponseObject<PagedResponseDTO<BranchDTO>>> getAllBranches(
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        // TODO: Implement get all branches logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Branches retrieved successfully", null)
        );
    }

    /**
     * Get Branch Detail
     * GET /branches/{id}
     *
     * @param id Branch ID
     * @return Detailed branch information including time slots and resources
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<BranchDetailDTO>> getBranchById(@PathVariable Long id) {
        // TODO: Implement get branch by ID logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Branch retrieved successfully", null)
        );
    }

    /**
     * Create Branch
     * POST /branches
     * Roles: MANAGER, ADMIN
     *
     * @param request Branch creation data
     * @return Created branch information
     */
    @PostMapping
    public ResponseEntity<ResponseObject<BranchDTO>> createBranch(
            @Valid @RequestBody CreateBranchRequestDTO request) {
        // TODO: Implement create branch logic
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Branch created successfully", null)
        );
    }

    /**
     * Update Branch
     * PUT /branches/{id}
     * Roles: MANAGER, ADMIN
     *
     * @param id      Branch ID
     * @param request Branch update data
     * @return Updated branch information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<BranchDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequestDTO request) {
        // TODO: Implement update branch logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Branch updated successfully", null)
        );
    }

    /**
     * Delete Branch
     * DELETE /branches/{id}
     * Roles: MANAGER, ADMIN
     *
     * @param id Branch ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        // TODO: Implement delete branch logic
        return ResponseEntity.noContent().build();
    }
}
