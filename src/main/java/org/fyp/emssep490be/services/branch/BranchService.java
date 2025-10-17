package org.fyp.emssep490be.services.branch;

import org.fyp.emssep490be.dtos.branch.BranchDTO;
import org.fyp.emssep490be.dtos.branch.BranchDetailDTO;
import org.fyp.emssep490be.dtos.branch.CreateBranchRequestDTO;
import org.fyp.emssep490be.dtos.branch.UpdateBranchRequestDTO;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;

/**
 * Service interface for Branch management operations
 */
public interface BranchService {

    /**
     * Get all branches with pagination and filtering
     *
     * @param centerId Filter by center ID
     * @param status   Filter by status
     * @param page     Page number
     * @param limit    Items per page
     * @return Paginated list of branches
     */
    PagedResponseDTO<BranchDTO> getAllBranches(Long centerId, String status, Integer page, Integer limit);

    /**
     * Get branch by ID with detailed information
     *
     * @param id Branch ID
     * @return Branch details including time slots and resources
     */
    BranchDetailDTO getBranchById(Long id);

    /**
     * Create a new branch
     *
     * @param request Branch creation data
     * @return Created branch information
     */
    BranchDTO createBranch(CreateBranchRequestDTO request);

    /**
     * Update an existing branch
     *
     * @param id      Branch ID
     * @param request Branch update data
     * @return Updated branch information
     */
    BranchDTO updateBranch(Long id, UpdateBranchRequestDTO request);

    /**
     * Delete a branch
     *
     * @param id Branch ID
     */
    void deleteBranch(Long id);
}
