package org.fyp.emssep490be.services.classmanagement;

import org.fyp.emssep490be.dtos.classmanagement.*;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.ApprovalRequestDTO;

import java.time.LocalDate;

/**
 * Service interface for Class Management operations
 */
public interface ClassManagementService {

    /**
     * Get all classes with pagination and filtering
     *
     * @param branchId      Filter by branch ID
     * @param courseId      Filter by course ID
     * @param status        Filter by status
     * @param modality      Filter by modality
     * @param startDateFrom Filter by start date from
     * @param startDateTo   Filter by start date to
     * @param page          Page number
     * @param limit         Items per page
     * @return Paginated list of classes
     */
    PagedResponseDTO<ClassDTO> getAllClasses(Long branchId, Long courseId, String status,
                                             String modality, LocalDate startDateFrom,
                                             LocalDate startDateTo, Integer page, Integer limit);

    /**
     * Get class by ID with detailed information
     *
     * @param id Class ID
     * @return Detailed class information
     */
    ClassDetailDTO getClassById(Long id);

    /**
     * Create a new class and auto-generate sessions from course template
     *
     * @param request Class creation data
     * @return Created class information with sessions count
     */
    CreateClassResponseDTO createClass(CreateClassRequestDTO request);

    /**
     * Submit class for approval
     *
     * @param id Class ID
     * @return Submitted class information
     */
    ClassDTO submitClassForApproval(Long id);

    /**
     * Approve or reject a class
     *
     * @param id      Class ID
     * @param request Approval action with optional rejection reason
     * @return Approved/rejected class information
     */
    ClassDTO approveClass(Long id, ApprovalRequestDTO request);

    /**
     * Update class schedule for future sessions
     *
     * @param id      Class ID
     * @param request Schedule update data
     * @return Update result with sessions count and conflicts
     */
    UpdateClassScheduleResponseDTO updateClassSchedule(Long id, UpdateClassScheduleRequestDTO request);

    /**
     * Validate class schedule for conflicts and warnings
     *
     * @param id Class ID
     * @return Validation result with conflicts and warnings
     */
    ClassValidationResponseDTO validateClassSchedule(Long id);

    /**
     * Delete a class
     *
     * @param id Class ID
     */
    void deleteClass(Long id);
}
